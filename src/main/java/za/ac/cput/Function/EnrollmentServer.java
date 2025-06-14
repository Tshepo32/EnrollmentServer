package za.ac.cput.Function;

import za.ac.cput.DAO.AdminDAO;
import za.ac.cput.DAO.CourseDAO;
import za.ac.cput.DAO.StudentDAO;
import za.ac.cput.DAO.UserDAO;
import za.ac.cput.DAO.EnrollmentDAO;
import za.ac.cput.Domain.*;

import javax.swing.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EnrollmentServer {

    private static ServerSocket serverSocket;
    private static final Logger logger = Logger.getLogger(EnrollmentServer.class.getName());
    private AdminDAO adminDAO;
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;
    private UserDAO userDAO;
    private EnrollmentDAO enrollmentDAO;

    public EnrollmentServer() throws ClassNotFoundException {
        int port = 12345;
        try {
            serverSocket = new ServerSocket(port);
            JOptionPane.showMessageDialog(null, "Server is listening on port " + port, "Server Status", JOptionPane.INFORMATION_MESSAGE);

            // Initialize DAOs
            try {
                adminDAO = new AdminDAO();
                studentDAO = new StudentDAO();
                courseDAO = new CourseDAO();
                userDAO = new UserDAO();
                enrollmentDAO = new EnrollmentDAO();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to initialize DAOs due to SQLException", e);
                // Exit or handle this fatal error if DAOs can't connect
                System.exit(1);
            }

            while (true) {
                // Server listens for incoming client connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from: " + clientSocket.getInetAddress().getHostAddress());

                // Handle each client in a new thread
                new Thread(() -> {
                    ObjectOutputStream clientOut = null;
                    ObjectInputStream clientIn = null;
                    try {
                        // Get streams for this specific client
                        clientOut = new ObjectOutputStream(clientSocket.getOutputStream());
                        clientOut.flush();
                        clientIn = new ObjectInputStream(clientSocket.getInputStream());
                        System.out.println("Streams successfully created for client " + clientSocket.getInetAddress().getHostAddress());

                        // Process requests for this client until they disconnect
                        processClientRequests(clientOut, clientIn, clientSocket);

                    } catch (EOFException e) {
                        System.out.println("Client disconnected gracefully: " + clientSocket.getInetAddress().getHostAddress());
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "IO error with client " + clientSocket.getInetAddress().getHostAddress(), e);
                    } catch (ClassNotFoundException e) {
                        logger.log(Level.SEVERE, "Class not found error with client " + clientSocket.getInetAddress().getHostAddress(), e);
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "Database error during client processing for " + clientSocket.getInetAddress().getHostAddress(), e);
                    } finally {
                        // --- FIX: Ensure client resources are closed for THIS client ---
                        try {
                            if (clientOut != null) clientOut.close();
                            if (clientIn != null) clientIn.close();
                            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
                            System.out.println("Client connection closed: " + clientSocket.getInetAddress().getHostAddress());
                        } catch (IOException e) {
                            logger.log(Level.WARNING, "Error closing client resources for " + clientSocket.getInetAddress().getHostAddress(), e);
                        }
                    }
                }).start(); // Start the new thread
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Server socket error", ex);
        } finally {
            // Ensure server socket is closed when the server application shuts down
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    System.out.println("Server socket closed.");
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error closing server socket", ex);
            }
        }
    }

    // ProcessClient to accept streams for the current client
    public void processClientRequests(ObjectOutputStream clientOut, ObjectInputStream clientIn, Socket clientSocket) throws ClassNotFoundException, SQLException, IOException {
        System.out.println("Processing requests for client: " + clientSocket.getInetAddress().getHostAddress());

        // The while loop remains here to process multiple requests from a single connected client
        while (true) {
            Object receivedObject = clientIn.readObject(); // Read command

            if (receivedObject instanceof String) {
                String command = (String) receivedObject;
                System.out.println("Received command: " + command);

                // Use the passed clientOut/clientIn for all operations
                if (command.equals("ADMIN_LOGIN")) {
                    Admin receivedAdmin = (Admin) clientIn.readObject();
                    System.out.println("Received Admin: " + receivedAdmin);
                    boolean success = adminDAO.validateAdmin(receivedAdmin);
                    String response = success ? "Admin_LoggedIn" : "ADMIN_FAILED";
                    clientOut.writeObject(response);
                    clientOut.flush();
                    System.out.println("Sent response to client: " + response);
                } else if (command.equals("!ADMIN_LOGIN")) {
                    Login receivedLogin = (Login) clientIn.readObject();
                    System.out.println("Received Login: " + receivedLogin);
                    boolean success = userDAO.authenticateUser(receivedLogin);
                    String response = success ? "!Admin_LoggedIn" : "!ADMIN_FAILED";
                    clientOut.writeObject(response);
                    clientOut.flush();
                    System.out.println("Sent response to client: " + response);
                } else if (command.equals("SEND_STUDENT")) {
                    Student receivedStudent = (Student) clientIn.readObject();
                    System.out.println("Received Student: " + receivedStudent);
                    boolean success = adminDAO.addStudent(receivedStudent);
                    String response = success ? "STUDENT_ADDED" : "STUDENT_ADD_FAILED";
                    clientOut.writeObject(response);
                    clientOut.flush();
                    System.out.println("Sent response to client: " + response);
                } else if (command.equals("DELETE_STUDENT")) {
                    String studentNumber = (String) clientIn.readObject();
                    boolean success = adminDAO.deleteStudent(studentNumber);
                    String response = success ? "STUDENT_DELETED" : "STUDENT_DELETE_FAILED";
                    clientOut.writeObject(response);
                    clientOut.flush();
                    System.out.println("Sent response to client: " + response);
                } else if (command.equals("SEND_COURSE")) {
                    Course receivedCourse = (Course) clientIn.readObject();
                    System.out.println("Received Course: " + receivedCourse);
                    boolean success = courseDAO.addCourse(receivedCourse);
                    String response = success ? "COURSE_ADDED" : "COURSE_ADD_FAILED";
                    clientOut.writeObject(response);
                    clientOut.flush();
                    System.out.println("Sent response to client: " + response);
                } else if (command.equals("DELETE_COURSE")) {
                    String courseCode = (String) clientIn.readObject();
                    boolean success = courseDAO.deleteCourse(courseCode);
                    String response = success ? "COURSE_DELETED" : "COURSE_DELETE_FAILED";
                    clientOut.writeObject(response);
                    clientOut.flush();
                    System.out.println("Sent response to client: " + response);
                } else if (command.equals("SEND_USER")) {
                    User receivedUser = (User) clientIn.readObject();
                    System.out.println("Received user: " + receivedUser);
                    boolean success = userDAO.registerUser(receivedUser);
                    String response = success ? "USER_ADDED" : "USER_ADD_FAILED";
                    clientOut.writeObject(response);
                    clientOut.flush();
                    System.out.println("Sent response to client: " + response);
                } else if (command.equals("SEARCH_STUDENT")) {
                    String studentID = (String) clientIn.readObject();
                    System.out.println("Received Search Request for Student with ID: " + studentID);
                    Student searchedStudent = adminDAO.getStudentById(studentID); // Use studentID directly
                    clientOut.writeObject(searchedStudent);
                    clientOut.flush();
                    System.out.println("Sent Searched Student Details to Client: " + searchedStudent);
                } else if (command.equals("RETRIEVE_STUDENTS")) {
                    List<Student> studentList = adminDAO.getAllStudents();
                    clientOut.writeObject(studentList);
                    clientOut.flush();
                    System.out.println("Sent list of students to client.");
                } else if (command.equals("SEND_COURSES")) {
                    List<Course> courses = courseDAO.getAllCourses();
                    clientOut.writeObject(courses);
                    clientOut.flush();
                    System.out.println("Sent list of courses to client.");
                } else if (command.equals("ENROLL_STUDENT")) {
                    Enrollment enrollment = (Enrollment) clientIn.readObject();
                    System.out.println("Received Enroll Request: " + enrollment);
                    boolean enrollmentSuccessful = enrollmentDAO.enrollStudentInCourse(enrollment);
                    String response = enrollmentSuccessful ? "ENROLLMENT_SUCCESSFUL" : "ENROLLMENT_FAILED";
                    clientOut.writeObject(response);
                    clientOut.flush();
                    System.out.println("Sent Enrollment Response to Client: " + response);
                } else if (command.equals("UNENROLL_STUDENT")) {
                    // Read the full Enrollment object, just like for ENROLL_STUDENT
                    Enrollment enrollmentToUnenroll = (Enrollment) clientIn.readObject();
                    System.out.println("Received Unenroll Request: " + enrollmentToUnenroll); // Log the object

                    // Call the DAO method, which now returns a specific String response
                    String unenrollmentResponse = String.valueOf(enrollmentDAO.unenrollStudentFromCourse(enrollmentToUnenroll));

                    clientOut.writeObject(unenrollmentResponse); // Send the detailed response back to the client
                    clientOut.flush();
                    System.out.println("Sent Unenrollment Response to Client: " + unenrollmentResponse);
                } else if (command.equals("SEARCH_ENROLLMENTS_BY_STUDENT_ID")) {
                    String studentId = (String) clientIn.readObject();
                    System.out.println("Received Search Request for Student ID: " + studentId);

                    List<Enrollment> foundEnrollments = enrollmentDAO.getEnrollmentsByStudentId(studentId); // New DAO method

                    if (foundEnrollments != null && !foundEnrollments.isEmpty()) {
                        clientOut.writeObject("SEARCH_SUCCESS");
                        clientOut.writeObject(foundEnrollments);
                        System.out.println("Sent " + foundEnrollments.size() + " enrollments for student " + studentId);
                    } else {
                        // Determine if student exists first for more specific feedback
                        if (enrollmentDAO.isStudentExists(studentId)) { // Re-use or add this helper
                            clientOut.writeObject("NO_ENROLLMENTS_FOUND"); // Student exists, but no enrollments
                        } else {
                            clientOut.writeObject("STUDENT_NOT_FOUND"); // Student does not exist
                        }
                        System.out.println("No enrollments found for student " + studentId);
                    }
                    clientOut.flush();
                } else if (command.equals("RETRIEVE_ENROLLMENTS")) {
                    logger.info("Handling RETRIEVE_ENROLLMENTS command.");
                    List<Enrollment> allEnrollments = enrollmentDAO.getAllEnrollments();

                    if (allEnrollments != null && !allEnrollments.isEmpty()) {
                        clientOut.writeObject("ENROLLMENTS_SUCCESS");
                        clientOut.writeObject(allEnrollments);
                        logger.info("Sent ENROLLMENTS_SUCCESS and " + allEnrollments.size() + " enrollments.");
                    } else {
                        clientOut.writeObject("NO_ENROLLMENTS_FOUND");
                        logger.info("Sent NO_ENROLLMENTS_FOUND.");
                    }
                    clientOut.flush(); // Ensure data is sent immediately
                }
                else {
                    System.out.println("Unknown command: " + command);
                }
            }
        }
    }

    // You might want to provide a way to cleanly shut down the server
    public static void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error shutting down server socket", e);
        }
    }
}