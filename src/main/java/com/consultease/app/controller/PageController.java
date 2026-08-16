package com.consultease.app.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.consultease.app.model.Consultation;
import com.consultease.app.model.FacultySchedule;
import com.consultease.app.model.User;
import com.consultease.app.repository.ConsultationRepository;
import com.consultease.app.repository.FacultyScheduleRepository;
import com.consultease.app.repository.UserRepository;
import com.consultease.app.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PageController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private FacultyScheduleRepository facultyScheduleRepository;

    @GetMapping({"/", "/home"})
    public String showHomePage(Principal principal, HttpSession session, Model model) {
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                session.setAttribute("loggedInUser", user);
                model.addAttribute("loggedInUser", user);
            });
        }
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @RequestParam("email") String email,
            @RequestParam("firstName") String firstName,
            @RequestParam(value = "middleName", required = false) String middleName,
            @RequestParam("lastName") String lastName,
            @RequestParam(value = "suffix", required = false) String suffix,
            @RequestParam(value = "campus", required = false) String campus,
            @RequestParam(value = "course", required = false) String course,
            @RequestParam("idNumber") String idNumber,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match. Please try again.");
            return "register";
        }

        String emailRegex = "^[A-Za-z0-9._%+-]+\\.[A-Za-z0-9.-]+\\.sjc@phinmaed\\.com$";
        if (email == null || !email.matches(emailRegex)) {
            model.addAttribute("errorMessage", "Please use your official school email (e.g. name.surname.sjc@phinmaed.com)");
            return "register";
        }

        if (idNumber != null && !idNumber.trim().isEmpty()) {
            String idRegex = "^\\d{2}-\\d{4}-\\d{6}$";
            if (!idNumber.matches(idRegex)) {
                model.addAttribute("errorMessage", "Invalid ID format. Must follow 00-0000-000000.");
                return "register";
            }
        }

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("errorMessage", "An account with this email already exists.");
            return "register";
        }

        String otpCode = String.valueOf((int)(Math.random() * 900000) + 100000);
        
        try {
            emailService.sendOtpEmail(email, otpCode);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to send OTP email. Please try again later.");
            return "register";
        }

        session.setAttribute("temp_email", email);
        session.setAttribute("temp_firstName", firstName);
        session.setAttribute("temp_middleName", middleName);
        session.setAttribute("temp_lastName", lastName);
        session.setAttribute("temp_suffix", suffix);
        session.setAttribute("temp_campus", campus);
        session.setAttribute("temp_course", course);
        session.setAttribute("temp_idNumber", idNumber);
        session.setAttribute("temp_password", passwordEncoder.encode(password));
        session.setAttribute("generated_otp", otpCode);

        return "redirect:/otp-verify";
    }

    @GetMapping("/otp-verify")
    public String showOtpPage(HttpSession session, Model model) {
        if (session.getAttribute("generated_otp") == null) {
            return "redirect:/register";
        }
        model.addAttribute("maskedEmail", session.getAttribute("temp_email"));
        return "otp-verify";
    }

    @GetMapping("/otp-resend")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("temp_email");
        if (email == null) {
            return "redirect:/register";
        }

        String newOtpCode = String.valueOf((int)(Math.random() * 900000) + 100000);
        try {
            emailService.sendOtpEmail(email, newOtpCode);
            session.setAttribute("generated_otp", newOtpCode);
            redirectAttributes.addFlashAttribute("successMessage", "A new OTP code has been sent to your email.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to resend OTP email. Please try again.");
        }

        return "redirect:/otp-verify";
    }

    @PostMapping("/otp-verify")
    public String processOtp(@RequestParam("otp") String otp, HttpSession session, Model model) {
        String sessionOtp = (String) session.getAttribute("generated_otp");

        if (sessionOtp != null && sessionOtp.equals(otp.trim())) {
            String fName = (String) session.getAttribute("temp_firstName");
            String lName = (String) session.getAttribute("temp_lastName");
            String mName = (String) session.getAttribute("temp_middleName");
            String suffix = (String) session.getAttribute("temp_suffix");

            User newUser = new User();
            newUser.setEmail((String) session.getAttribute("temp_email"));
            newUser.setFirstName(fName);
            newUser.setMiddleName(mName);
            newUser.setLastName(lName);
            newUser.setSuffix(suffix);
            
            String full = fName + (mName != null && !mName.isEmpty() ? " " + mName : "") + " " + lName;
            if (suffix != null && !suffix.isEmpty()) {
                full += " " + suffix;
            }
            newUser.setFullName(full);

            newUser.setCourse((String) session.getAttribute("temp_course"));
            newUser.setCampus((String) session.getAttribute("temp_campus"));
            newUser.setIdNumber((String) session.getAttribute("temp_idNumber"));
            newUser.setPassword((String) session.getAttribute("temp_password"));
            newUser.setRole(User.Role.STUDENT);
            newUser.setIsVerified(true);

            userRepository.save(newUser);
            session.invalidate();
            return "redirect:/login?registered=true";
        }

        model.addAttribute("errorMessage", "Invalid OTP code. Please try again.");
        return "otp-verify";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        List<User> users = userRepository.findAll();
        List<Consultation> consultations = consultationRepository.findAllByOrderByIdDesc();
        
        long totalUsersCount = users.size();
        long facultyCount = users.stream().filter(u -> u.getRole() == User.Role.FACULTY).count();
        long totalConsultationsCount = consultations.size();
        long pendingConsultationsCount = consultations.stream()
                .filter(c -> "PENDING".equalsIgnoreCase(c.getStatus()))
                .count();

        model.addAttribute("adminName", "Dr. Melvin Soldevilla");
        model.addAttribute("users", users);
        model.addAttribute("consultations", consultations);
        model.addAttribute("totalUsersCount", totalUsersCount);
        model.addAttribute("facultyCount", facultyCount);
        model.addAttribute("totalConsultationsCount", totalConsultationsCount);
        model.addAttribute("pendingConsultationsCount", pendingConsultationsCount);

        return "admin-dashboard";
    }

    @PostMapping("/admin/create-faculty")
    public String createFacultyAccount(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("idNumber") String idNumber,
            @RequestParam("course") String course,
            @RequestParam(value = "role", defaultValue = "FACULTY") String roleStr,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {

        if (userRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User email already exists!");
            return "redirect:/admin/dashboard?error=exists";
        }

        try {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setIdNumber(idNumber);
            user.setCourse(course);
            user.setPassword(passwordEncoder.encode(password));
            user.setIsVerified(true);

            if ("ADMIN".equalsIgnoreCase(roleStr)) {
                user.setRole(User.Role.ADMIN);
            } else if ("STUDENT".equalsIgnoreCase(roleStr)) {
                user.setRole(User.Role.STUDENT);
            } else {
                user.setRole(User.Role.FACULTY);
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully!");
            return "redirect:/admin/dashboard?success=userCreated";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/dashboard?error=true";
        }
    }

    @PostMapping("/admin/update-user")
    public String updateUser(
            @RequestParam("userId") Long userId,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("idNumber") String idNumber,
            @RequestParam("course") String course,
            @RequestParam("role") String roleStr,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setFullName(fullName);
            user.setEmail(email);
            user.setIdNumber(idNumber);
            user.setCourse(course);

            if ("ADMIN".equalsIgnoreCase(roleStr)) {
                user.setRole(User.Role.ADMIN);
            } else if ("FACULTY".equalsIgnoreCase(roleStr)) {
                user.setRole(User.Role.FACULTY);
            } else {
                user.setRole(User.Role.STUDENT);
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/dashboard?success=userUpdated";
        }
        return "redirect:/admin/dashboard?error=notFound";
    }

    @PostMapping("/admin/delete-user")
    public String deleteUser(@RequestParam("userId") Long userId, RedirectAttributes redirectAttributes) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
            return "redirect:/admin/dashboard?success=userDeleted";
        }
        return "redirect:/admin/dashboard?error=notFound";
    }

    @PostMapping("/admin/update-consultation-status")
    public String updateAdminConsultationStatus(
            @RequestParam("consultationId") Long consultationId,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {

        Consultation consultation = consultationRepository.findById(consultationId).orElse(null);
        if (consultation != null) {
            consultation.setStatus(status.toUpperCase());
            consultationRepository.save(consultation);

            sendStatusUpdateEmailToStudent(consultation);

            redirectAttributes.addFlashAttribute("successMessage", "Consultation status updated!");
            return "redirect:/admin/dashboard?success=consultationUpdated";
        }
        return "redirect:/admin/dashboard?error=notFound";
    }

    @GetMapping("/faculty/dashboard")
    public String showFacultyDashboard(Principal principal, HttpSession session, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        User facultyUser = userRepository.findByEmail(principal.getName()).orElse(null);
        if (facultyUser == null) {
            return "redirect:/login";
        }

        session.setAttribute("loggedInUser", facultyUser);
        model.addAttribute("loggedInUser", facultyUser);
        model.addAttribute("profName", facultyUser.getFullName());

        String facultyKeyword = facultyUser.getLastName() != null ? facultyUser.getLastName() : facultyUser.getFullName();
        List<Consultation> consultations = consultationRepository.findByTargetHeadContainingOrderByIdDesc(facultyKeyword);
        List<FacultySchedule> schedules = facultyScheduleRepository.findByFaculty(facultyUser);

        long totalCount = consultationRepository.countByTargetHeadContaining(facultyKeyword);
        long pendingCount = consultationRepository.countByTargetHeadContainingAndStatus(facultyKeyword, "PENDING");
        long approvedCount = consultationRepository.countByTargetHeadContainingAndStatus(facultyKeyword, "APPROVED");
        long completedCount = consultationRepository.countByTargetHeadContainingAndStatus(facultyKeyword, "COMPLETED");

        model.addAttribute("consultations", consultations);
        model.addAttribute("schedules", schedules);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("completedCount", completedCount);

        return "faculty-dashboard";
    }

    @PostMapping("/faculty/approve-consultation")
    public String approveConsultation(
            @RequestParam("consultationId") Long consultationId,
            RedirectAttributes redirectAttributes) {

        Consultation consultation = consultationRepository.findById(consultationId).orElse(null);
        if (consultation != null) {
            consultation.setStatus("APPROVED");
            consultationRepository.save(consultation);

            sendStatusUpdateEmailToStudent(consultation);

            redirectAttributes.addFlashAttribute("successMessage", "Consultation request approved successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Request not found.");
        }

        return "redirect:/faculty/dashboard?success=approved";
    }

    @PostMapping("/faculty/decline-consultation")
    public String declineConsultation(
            @RequestParam("consultationId") Long consultationId,
            @RequestParam(value = "remarks", required = false) String remarks,
            RedirectAttributes redirectAttributes) {

        Consultation consultation = consultationRepository.findById(consultationId).orElse(null);
        if (consultation != null) {
            consultation.setStatus("DECLINED");
            if (remarks != null && !remarks.trim().isEmpty()) {
                consultation.setRemarks(remarks);
            }
            consultationRepository.save(consultation);

            sendStatusUpdateEmailToStudent(consultation);

            redirectAttributes.addFlashAttribute("successMessage", "Consultation request declined.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Request not found.");
        }

        return "redirect:/faculty/dashboard?success=declined";
    }

    @PostMapping("/faculty/update-request-status")
    public String updateRequestStatus(
            @RequestParam("consultationId") Long consultationId,
            @RequestParam("status") String status,
            @RequestParam(value = "remarks", required = false) String remarks,
            RedirectAttributes redirectAttributes) {

        Consultation consultation = consultationRepository.findById(consultationId).orElse(null);
        if (consultation != null) {
            consultation.setStatus(status.toUpperCase());
            if (remarks != null && !remarks.trim().isEmpty()) {
                consultation.setRemarks(remarks);
            }
            consultationRepository.save(consultation);

            sendStatusUpdateEmailToStudent(consultation);

            redirectAttributes.addFlashAttribute("successMessage", "Consultation request status updated!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Request not found.");
        }

        return "redirect:/faculty/dashboard?success=statusUpdated";
    }

    @PostMapping("/faculty/add-schedule")
    public String addFacultySchedule(
            @RequestParam("dayOfWeek") String dayOfWeek,
            @RequestParam("timeSlot") String timeSlot,
            @RequestParam("officeLocation") String officeLocation,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        User facultyUser = userRepository.findByEmail(principal.getName()).orElse(null);
        if (facultyUser != null) {
            FacultySchedule schedule = new FacultySchedule(facultyUser, dayOfWeek, timeSlot, officeLocation);
            facultyScheduleRepository.save(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "New schedule slot added successfully!");
        }

        return "redirect:/faculty/dashboard?success=scheduleAdded";
    }

    @PostMapping("/faculty/delete-schedule")
    public String deleteFacultySchedule(
            @RequestParam("scheduleId") Long scheduleId,
            RedirectAttributes redirectAttributes) {

        if (facultyScheduleRepository.existsById(scheduleId)) {
            facultyScheduleRepository.deleteById(scheduleId);
            redirectAttributes.addFlashAttribute("successMessage", "Schedule slot deleted successfully!");
        }

        return "redirect:/faculty/dashboard?success=scheduleDeleted";
    }

    @GetMapping("/student/dashboard")
    public String showDashboard(
            @RequestParam(value = "dept", required = false) String dept,
            @RequestParam(value = "search", required = false) String search,
            Principal principal,
            HttpSession session,
            Model model) {

        User loggedInUser = null;

        if (principal != null) {
            String email = principal.getName();
            loggedInUser = userRepository.findByEmail(email).orElse(null);
            if (loggedInUser != null) {
                session.setAttribute("loggedInUser", loggedInUser);
            }
        }

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if ((dept == null || dept.trim().isEmpty()) && loggedInUser.getCourse() != null) {
            dept = mapCourseToDepartment(loggedInUser.getCourse());
        }

        List<User> facultyList = userRepository.filterFaculty(dept, search);
        List<Map<String, String>> availableSubjects = getSubjectsByCourse(loggedInUser.getCourse());

        List<Consultation> consultations = consultationRepository.findByUserOrderByIdDesc(loggedInUser);
        List<FacultySchedule> allSchedules = facultyScheduleRepository.findAll();

        long totalCount = consultationRepository.countByUser(loggedInUser);
        long pendingCount = consultationRepository.countByUserAndStatus(loggedInUser, "PENDING");
        long approvedCount = consultationRepository.countByUserAndStatus(loggedInUser, "APPROVED");

        Consultation nextUpcoming = consultations.stream()
                .filter(c -> "APPROVED".equalsIgnoreCase(c.getStatus()))
                .findFirst()
                .orElse(null);

        String fullCourseName = mapCourseToFullTitle(loggedInUser.getCourse());

        model.addAttribute("facultyList", facultyList);
        model.addAttribute("availableSubjects", availableSubjects);
        model.addAttribute("selectedDept", dept);
        model.addAttribute("searchTerm", search);
        model.addAttribute("userCourse", loggedInUser.getCourse() != null ? loggedInUser.getCourse() : "");
        model.addAttribute("fullCourseName", fullCourseName);

        model.addAttribute("consultations", consultations);
        model.addAttribute("schedules", allSchedules);
        model.addAttribute("nextUpcoming", nextUpcoming);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);

        return "student-dashboard";
    }

    // 📮 BOOK CONSULTATION WITH FILE ATTACHMENT, TEACHER NOTIFICATION & STUDENT SUBMISSION CONFIRMATION
    @PostMapping("/book-consultation")
    public String bookConsultation(
            @RequestParam("targetHead") String targetHead,
            @RequestParam("preferredDate") String preferredDate,
            @RequestParam("preferredTime") String preferredTime,
            @RequestParam("purpose") String purpose,
            @RequestParam(value = "meetingMode", defaultValue = "In-Person") String meetingMode,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        User loggedInUser = userRepository.findByEmail(principal.getName()).orElse(null);
        if (loggedInUser == null) return "redirect:/login";

        try {
            List<Consultation> existingList = consultationRepository.findAll();
            boolean isConflict = existingList.stream().anyMatch(c ->
                    targetHead.equalsIgnoreCase(c.getTargetHead()) &&
                    preferredDate.equalsIgnoreCase(c.getPreferredDate()) &&
                    preferredTime.equalsIgnoreCase(c.getPreferredTime()) &&
                    ("PENDING".equalsIgnoreCase(c.getStatus()) || "APPROVED".equalsIgnoreCase(c.getStatus()))
            );

            if (isConflict) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "This time slot is already booked or pending for " + targetHead + ". Please choose another slot.");
                return "redirect:/student/dashboard?error=doubleBooked";
            }

            Consultation consultation = new Consultation();
            consultation.setUser(loggedInUser);
            consultation.setTargetHead(targetHead);
            consultation.setPreferredDate(preferredDate);
            consultation.setPreferredTime(preferredTime);
            consultation.setPurpose(purpose);
            consultation.setMeetingMode(meetingMode);
            consultation.setStatus("PENDING");

            if (attachment != null && !attachment.isEmpty()) {
                String uploadDir = "./uploads/attachments/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileExtension = attachment.getOriginalFilename().substring(attachment.getOriginalFilename().lastIndexOf("."));
                String fileName = "attachment_" + System.currentTimeMillis() + fileExtension;
                Path filePath = Paths.get(uploadDir + fileName);
                Files.copy(attachment.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                consultation.setAttachmentPath("/uploads/attachments/" + fileName);
            }

            consultationRepository.save(consultation);

            // 1️⃣ Send Email Notification to Teacher
            try {
                String teacherEmail = null;
                List<User> allUsers = userRepository.findAll();
                for (User u : allUsers) {
                    if (u.getRole() == User.Role.FACULTY) {
                        if ((u.getFullName() != null && targetHead.contains(u.getFullName())) ||
                            (u.getLastName() != null && targetHead.contains(u.getLastName()))) {
                            teacherEmail = u.getEmail();
                            break;
                        }
                    }
                }

                if (teacherEmail != null && !teacherEmail.isEmpty()) {
                    emailService.sendConsultationNotification(
                        teacherEmail,
                        loggedInUser.getFullName(),
                        preferredDate,
                        preferredTime,
                        purpose
                    );
                }
            } catch (Exception mailEx) {
                System.err.println("⚠️ Warning: Could not send email notification to teacher: " + mailEx.getMessage());
            }

            // 2️⃣ NEW: Send Submission Confirmation Email to Student
            try {
                if (loggedInUser.getEmail() != null) {
                    emailService.sendConsultationSubmittedEmail(
                        loggedInUser.getEmail(),
                        loggedInUser.getFullName(),
                        targetHead,
                        preferredDate,
                        preferredTime,
                        purpose
                    );
                }
            } catch (Exception studentMailEx) {
                System.err.println("⚠️ Warning: Could not send submission confirmation email to student: " + studentMailEx.getMessage());
            }

            redirectAttributes.addFlashAttribute("successMessage", "Consultation request submitted successfully!");
            return "redirect:/student/dashboard?success=booked";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit request.");
            return "redirect:/student/dashboard?error=true";
        }
    }

    @PostMapping("/student/cancel-consultation")
    public String cancelConsultation(
            @RequestParam("consultationId") Long consultationId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        User loggedInUser = userRepository.findByEmail(principal.getName()).orElse(null);
        Consultation consultation = consultationRepository.findById(consultationId).orElse(null);

        if (consultation != null && loggedInUser != null && consultation.getUser().getId().equals(loggedInUser.getId())) {
            if ("PENDING".equalsIgnoreCase(consultation.getStatus())) {
                consultation.setStatus("CANCELLED");
                consultationRepository.save(consultation);
                redirectAttributes.addFlashAttribute("successMessage", "Consultation request cancelled successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Only pending requests can be cancelled.");
            }
        }

        return "redirect:/student/dashboard?success=cancelled";
    }

    @PostMapping("/update-profile-picture")
    public String updateProfilePicture(
            @RequestParam("profileImage") MultipartFile file,
            Principal principal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (principal == null || file.isEmpty()) {
            return "redirect:/login";
        }

        try {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                String uploadDir = "./uploads/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = "user_" + user.getId() + "_" + System.currentTimeMillis() + ".png";
                Path filePath = Paths.get(uploadDir + fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                user.setProfilePicture("/uploads/" + fileName);
                userRepository.save(user);

                session.setAttribute("loggedInUser", user);
                redirectAttributes.addFlashAttribute("successMessage", "Profile picture updated successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload image.");
        }

        return "redirect:/student/dashboard?success=profileUpdated";
    }

    private void sendStatusUpdateEmailToStudent(Consultation consultation) {
        try {
            if (consultation != null && consultation.getUser() != null) {
                User student = consultation.getUser();
                if (student.getEmail() != null) {
                    emailService.sendConsultationStatusUpdateEmail(
                        student.getEmail(),
                        student.getFullName(),
                        consultation.getTargetHead(),
                        consultation.getStatus(),
                        consultation.getPreferredDate(),
                        consultation.getPreferredTime(),
                        consultation.getRemarks()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not send status update email to student: " + e.getMessage());
        }
    }

    private List<Map<String, String>> getSubjectsByCourse(String course) {
        List<Map<String, String>> list = new ArrayList<>();
        if (course == null) return list;
        
        String cleanCourse = course.replaceAll("[\\s-]", "").toUpperCase();

        if (cleanCourse.contains("BSN") || cleanCourse.contains("NURSING")) {
            list.add(createSubjectMap("NUR 101", "ANATOMY AND PHYSIOLOGY FOR NURSES", "Dr. Melvin Soldevilla"));
            list.add(createSubjectMap("NUR 102", "FUNDAMENTALS OF NURSING PRACTICE", "Prof. Maria Santos"));
            list.add(createSubjectMap("GEN 003", "SCIENCE TECHNOLOGY AND SOCIETY", "Mark Joshua Bodoy Vidar"));
            list.add(createSubjectMap("HIS 007", "LIFE AND WORKS OF RIZAL", "Henry James Herrera Bautista"));
        } else {
            list.add(createSubjectMap("ITE 300", "OBJECT ORIENTED PROGRAMMING", "Angel Mae Sevilla Galario"));
            list.add(createSubjectMap("ITE 298", "INFORMATION MANAGEMENT", "Angelo Jeric Balatbag Trias"));
            list.add(createSubjectMap("ITE 292", "NETWORKING 1", "Sigfried Fajardo Breton"));
            list.add(createSubjectMap("ITE 031", "DATA STRUCTURES AND ALGORITHMS", "Mark Anthony Porras Cezar"));
            list.add(createSubjectMap("ITE 083", "IT PROJECT MANAGEMENT", "Renjun A. Orain"));
            list.add(createSubjectMap("GEN 003", "SCIENCE TECHNOLOGY AND SOCIETY", "Mark Joshua Bodoy Vidar"));
            list.add(createSubjectMap("HIS 007", "LIFE AND WORKS OF RIZAL", "Henry James Herrera Bautista"));
        }
        return list;
    }

    private Map<String, String> createSubjectMap(String code, String desc, String teacher) {
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        map.put("description", desc);
        map.put("teacher", teacher);
        map.put("valueStr", code + " - " + teacher);
        map.put("textStr", code + " – " + desc + " (" + teacher + ")");
        return map;
    }

    private String mapCourseToDepartment(String course) {
        if (course == null || course.trim().isEmpty()) return "";
        String cleanCourse = course.replaceAll("[\\s-]", "").toUpperCase();

        switch (cleanCourse) {
            case "BSIT": case "BSCS": case "ACT":
                return "College of Information Technology";
            case "BSN": case "BSMLS": case "BSMT": case "BSPHARM": case "BSRT": case "BSPT":
                return "College of Nursing & Allied Health";
            case "BSCRIM":
                return "College of Criminology";
            case "BSBA": case "BSA": case "BSMA": case "BSHM": case "BSTM":
                return "College of Management & Accountancy";
            case "BSED": case "BEED": case "BSNED":
                return "College of Education";
            case "BSPSYCH": case "BSPSYCHOLOGY": case "BACOMM": case "BAPOLSCI":
                return "College of Arts & Sciences";
            case "BSCE": case "BSEE": case "BSME": case "BSCPE": case "BSARCH":
                return "College of Engineering & Architecture";
            default:
                return "";
        }
    }

    private String mapCourseToFullTitle(String course) {
        if (course == null || course.trim().isEmpty()) return "General Student";
        String cleanCourse = course.replaceAll("[\\s-]", "").toUpperCase();

        switch (cleanCourse) {
            case "BSN": return "Bachelor of Science in Nursing";
            case "BSIT": return "Bachelor of Science in Information Technology";
            case "BSCS": return "Bachelor of Science in Computer Science";
            case "ACT": return "Associate in Computer Technology";
            case "BSMLS": return "Bachelor of Science in Medical Laboratory Science";
            case "BSMT": return "Bachelor of Science in Medical Technology";
            case "BSPHARM": return "Bachelor of Science in Pharmacy";
            case "BSPT": return "Bachelor of Science in Physical Therapy";
            case "BSRT": return "Bachelor of Science in Radiologic Technology";
            case "BSCRIM": return "Bachelor of Science in Criminology";
            case "BSBA": return "Bachelor of Science in Business Administration";
            case "BSA": return "Bachelor of Science in Accountancy";
            case "BSMA": return "Bachelor of Science in Management Accounting";
            case "BSHM": return "Bachelor of Science in Hospitality Management";
            case "BSTM": return "Bachelor of Science in Tourism Management";
            case "BSED": return "Bachelor of Secondary Education";
            case "BEED": return "Bachelor of Elementary Education";
            case "BSNED": return "Bachelor of Special Needs Education";
            case "BSPSYCH": case "BSPSYCHOLOGY": return "Bachelor of Science in Psychology";
            case "BACOMM": return "Bachelor of Arts in Communication";
            case "BAPOLSCI": return "Bachelor of Arts in Political Science";
            case "BSCE": return "Bachelor of Science in Civil Engineering";
            case "BSCPE": return "Bachelor of Science in Computer Engineering";
            case "BSEE": return "Bachelor of Science in Electrical Engineering";
            case "BSME": return "Bachelor of Science in Mechanical Engineering";
            case "BSARCH": return "Bachelor of Science in Architecture";
            default: return course;
        }
    }
}