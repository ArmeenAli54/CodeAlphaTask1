import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class StudentGradeTracker extends JFrame {

    private ArrayList<Student> students = new ArrayList<>();
    private DefaultTableModel model;

    private JTextField nameField, marksField;

    private static final String USERS_FILE = "users.txt";
    private static final String DATA_FILE = "students.txt";

    // ================= MAIN =================
    public static void main(String[] args) {
        ensureFiles();
        SwingUtilities.invokeLater(LoginFrame::new);
    }

    // ================= AUTO FILES =================
    private static void ensureFiles() {
        try {
            File u = new File(USERS_FILE);
            if (!u.exists()) {
                PrintWriter pw = new PrintWriter(u);
                pw.println("admin,1234");
                pw.println("teacher,pass");
                pw.close();
            }

            File d = new File(DATA_FILE);
            if (!d.exists()) d.createNewFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOGIN WINDOW (UPDATED UI) =================
    static class LoginFrame extends JFrame {

        JTextField user;
        JPasswordField pass;

        LoginFrame() {

            setTitle("Login");
            setSize(420, 320);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE);

            JPanel p = new JPanel(null);
            p.setBackground(new Color(18, 24, 38));
            setContentPane(p);

            // ⭐ TITLE
            JLabel title = new JLabel("STUDENT GRADE TRACKER");
            title.setBounds(80, 20, 300, 30);
            title.setForeground(new Color(0, 123, 255));
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            p.add(title);

            // ⭐ SUBTITLE
            JLabel sub = new JLabel("LOGIN SYSTEM");
            sub.setBounds(160, 60, 200, 20);
            sub.setForeground(Color.WHITE);
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            p.add(sub);

            // LABELS
            JLabel l1 = new JLabel("Username:");
            JLabel l2 = new JLabel("Password:");

            l1.setForeground(Color.WHITE);
            l2.setForeground(Color.WHITE);

            l1.setBounds(50, 110, 100, 25);
            l2.setBounds(50, 150, 100, 25);

            p.add(l1);
            p.add(l2);

            // INPUTS
            user = new JTextField();
            pass = new JPasswordField();

            user.setBounds(150, 110, 200, 25);
            pass.setBounds(150, 150, 200, 25);

            p.add(user);
            p.add(pass);

            // LOGIN BUTTON
            JButton login = new JButton("LOGIN");
            login.setBounds(150, 200, 120, 35);
            login.setBackground(new Color(0, 123, 255));
            login.setForeground(Color.WHITE);

            p.add(login);

            login.addActionListener(e -> check());

            setVisible(true);
        }

        void check() {
            try (Scanner sc = new Scanner(new File(USERS_FILE))) {
                while (sc.hasNextLine()) {
                    String[] d = sc.nextLine().split(",");
                    if (d[0].equals(user.getText())
                            && d[1].equals(new String(pass.getPassword()))) {

                        dispose();
                        new StudentGradeTracker();
                        return;
                    }
                }
            } catch (Exception ignored) {}

            JOptionPane.showMessageDialog(this, "Invalid Login");
        }
    }

    // ================= MAIN DASHBOARD =================
    public StudentGradeTracker() {

        setTitle("Student Grade Tracker (GPA System)");
        setSize(850, 500);

        // ⭐ CENTER MAIN WINDOW
        setLocationRelativeTo(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Color bg = new Color(18, 24, 38);
        Color panel = new Color(28, 38, 58);
        Color accent = new Color(0, 123, 255);

        JPanel root = new JPanel(null);
        root.setBackground(bg);
        setContentPane(root);

        JLabel title = new JLabel("STUDENT GPA SYSTEM");
        title.setBounds(300, 10, 300, 30);
        title.setForeground(accent);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        root.add(title);

        // INPUT PANEL
        JPanel input = new JPanel(null);
        input.setBackground(panel);
        input.setBounds(20, 60, 300, 160);
        root.add(input);

        input.add(label("Name:", 10, 20));
        input.add(label("Marks:", 10, 60));

        nameField = new JTextField();
        marksField = new JTextField();

        nameField.setBounds(90, 20, 180, 25);
        marksField.setBounds(90, 60, 180, 25);

        input.add(nameField);
        input.add(marksField);

        JButton add = button("ADD", 90, 100, new Color(0, 123, 255));
        JButton del = button("DELETE", 180, 100, new Color(220, 53, 69));

        input.add(add);
        input.add(del);

        // REPORT BUTTON
        JButton report = new JButton("REPORT");
        report.setBounds(20, 240, 300, 40);
        report.setBackground(new Color(40, 167, 69));
        report.setForeground(Color.WHITE);
        root.add(report);

        // TABLE
        model = new DefaultTableModel(
                new Object[]{"Name", "Marks", "Grade", "GPA"}, 0);

        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(340, 60, 470, 350);
        root.add(sp);

        load();
        refresh();

        add.addActionListener(e -> addStudent());
        del.addActionListener(e -> deleteStudent(table));
        report.addActionListener(e -> new ReportWindow());

        setVisible(true);
    }

    // ================= REPORT WINDOW =================
    class ReportWindow extends JFrame {

        ReportWindow() {
            setTitle("Report Dashboard");
            setSize(400, 320);

            // ⭐ CENTER REPORT WINDOW
            setLocationRelativeTo(null);

            JPanel p = new JPanel(new GridLayout(6, 1));
            p.setBackground(new Color(18, 24, 38));

            addLabel(p, "Total Students: " + students.size());
            addLabel(p, "Average Marks: " + avgMarks());
            addLabel(p, "Highest Marks: " + maxMarks());
            addLabel(p, "Lowest Marks: " + minMarks());
            addLabel(p, "Average GPA: " + avgGPA());

            setContentPane(p);
            setVisible(true);
        }

        void addLabel(JPanel p, String text) {
            JLabel l = new JLabel(text);
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 14));
            p.add(l);
        }
    }

    // ================= ADD =================
    void addStudent() {
        try {
            students.add(new Student(
                    nameField.getText(),
                    Double.parseDouble(marksField.getText())
            ));

            save();
            refresh();

            nameField.setText("");
            marksField.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid data!");
        }
    }

    // ================= DELETE =================
    void deleteStudent(JTable table) {
        int r = table.getSelectedRow();
        if (r >= 0) {
            students.remove(r);
            save();
            refresh();
        }
    }

    // ================= TABLE =================
    void refresh() {
        model.setRowCount(0);

        for (Student s : students) {
            model.addRow(new Object[]{
                    s.name,
                    s.marks,
                    s.grade,
                    s.gpa
            });
        }
    }

    // ================= FILE SAVE =================
    void save() {
        try (PrintWriter pw = new PrintWriter(DATA_FILE)) {
            for (Student s : students) {
                pw.println(s.name + "," + s.marks + "," + s.grade + "," + s.gpa);
            }
        } catch (Exception ignored) {}
    }

    // ================= FILE LOAD =================
    void load() {
        students.clear();

        try (Scanner sc = new Scanner(new File(DATA_FILE))) {
            while (sc.hasNextLine()) {
                String[] d = sc.nextLine().split(",");
                students.add(new Student(d[0], Double.parseDouble(d[1])));
            }
        } catch (Exception ignored) {}
    }

    // ================= STATS =================
    double avgMarks() {
        if (students.isEmpty()) return 0;
        double s = 0;
        for (Student x : students) s += x.marks;
        return s / students.size();
    }

    double maxMarks() {
        double m = 0;
        for (Student x : students) m = Math.max(m, x.marks);
        return m;
    }

    double minMarks() {
        if (students.isEmpty()) return 0;
        double m = students.get(0).marks;
        for (Student x : students) m = Math.min(m, x.marks);
        return m;
    }

    double avgGPA() {
        if (students.isEmpty()) return 0;
        double s = 0;
        for (Student x : students) s += x.gpa;
        return s / students.size();
    }

    // ================= UI HELPERS =================
    JLabel label(String t, int x, int y) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.WHITE);
        l.setBounds(x, y, 80, 20);
        return l;
    }

    JButton button(String t, int x, int y, Color c) {
        JButton b = new JButton(t);
        b.setBounds(x, y, 80, 30);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        return b;
    }

    // ================= STUDENT MODEL =================
    static class Student {
        String name;
        double marks;
        String grade;
        double gpa;

        Student(String n, double m) {
            name = n;
            marks = m;
            calculate();
        }

        void calculate() {
            if (marks >= 90) { grade = "A+"; gpa = 4.0; }
            else if (marks >= 80) { grade = "A"; gpa = 3.7; }
            else if (marks >= 70) { grade = "B"; gpa = 3.0; }
            else if (marks >= 60) { grade = "C"; gpa = 2.3; }
            else if (marks >= 50) { grade = "D"; gpa = 2.0; }
            else { grade = "F"; gpa = 0.0; }
        }
    }
}