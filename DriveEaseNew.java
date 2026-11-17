import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class DriveEaseNew extends JFrame {

    ArrayList<Customer> customers = new ArrayList<>();
    ArrayList<Car> cars = new ArrayList<>();
    ArrayList<Booking> bookings = new ArrayList<>();

    int nextCustomerId = 1, nextBookingId = 1001;

    JTextArea output = new JTextArea(8, 50);
    DefaultTableModel carModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Type", "Price/Day", "Available"}, 0);
    JTable carTable = new JTable(carModel);

    JTextField name = new JTextField(15);
    JTextField phone = new JTextField(15);
    JTextField license = new JTextField(15);

    JTextField customerId = new JTextField(8);
    JTextField carId = new JTextField(8);
    JTextField days = new JTextField(8);

    JTextField paymentBookingId = new JTextField(8);
    JTextField returnBookingId = new JTextField(8);
    JTextField actualDays = new JTextField(8);

    static class Customer {
        int id;
        String name, phone, license;

        Customer(int id, String name, String phone, String license) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.license = license;
        }
    }

    static class Car {
        int id;
        String name, type;
        double price;
        boolean available = true;

        Car(int id, String name, String type, double price) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.price = price;
        }
    }

    static class Booking {
        int id, days;
        Customer customer;
        Car car;
        double amount;
        boolean paid = false;
        boolean handedOver = false;
        boolean returned = false;

        Booking(int id, Customer customer, Car car,
                int days, double amount) {
            this.id = id;
            this.customer = customer;
            this.car = car;
            this.days = days;
            this.amount = amount;
        }
    }

    DriveEaseNew() {
        setTitle("Car Rental Management System");
        setSize(1050, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addCars();

        // Place tabs vertically so every module name stays visible.
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.addTab("1. Registration", registrationPanel());
        tabs.addTab("2. Vehicle Search", vehiclePanel());
        tabs.addTab("3. Booking", bookingPanel());
        tabs.addTab("4. Payment & Handover", paymentPanel());
        tabs.addTab("5. Rental / Late Fee", rentalPanel());
        tabs.addTab("6. Vehicle Return / Final Payment", returnPanel());

        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));

        add(tabs, BorderLayout.CENTER);
        add(new JScrollPane(output), BorderLayout.SOUTH);

        refreshCars();
    }

    void addCars() {
        cars.add(new Car(1, "Swift", "Hatchback", 1500));
        cars.add(new Car(2, "Honda City", "Sedan", 2500));
        cars.add(new Car(3, "Innova", "SUV", 3500));
        cars.add(new Car(4, "Baleno", "Hatchback", 1800));
        cars.add(new Car(5, "Creta", "SUV", 3000));
    }

    JPanel makePanel() {
        return new JPanel(new GridBagLayout());
    }

    void addField(JPanel p, String label, JTextField field, int row) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.gridx = 0;
        g.gridy = row;
        g.anchor = GridBagConstraints.WEST;
        p.add(new JLabel(label), g);

        g.gridx = 1;
        p.add(field, g);
    }

    void show(String message) {
        output.append(message + "\n");
    }

    void refreshCars() {
        carModel.setRowCount(0);
        for (Car c : cars) {
            carModel.addRow(new Object[]{
                c.id, c.name, c.type, c.price,
                c.available ? "Yes" : "No"
            });
        }
    }

    Customer findCustomer(int id) {
        for (Customer c : customers)
            if (c.id == id) return c;
        return null;
    }

    Car findCar(int id) {
        for (Car c : cars)
            if (c.id == id) return c;
        return null;
    }

    Booking findBooking(int id) {
        for (Booking b : bookings)
            if (b.id == id) return b;
        return null;
    }

    int number(String text) {
        return Integer.parseInt(text.trim());
    }

    double amount(String text) {
        return Double.parseDouble(text.trim());
    }

    // MODULE 1: Customer Registration
    JPanel registrationPanel() {
        JPanel p = makePanel();

        addField(p, "Customer Name:", name, 0);
        addField(p, "Phone Number:", phone, 1);
        addField(p, "Driving Licence:", license, 2);

        JButton register = new JButton("Register Customer");
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = 3;
        g.insets = new Insets(8, 8, 8, 8);
        p.add(register, g);

        register.addActionListener(e -> {
            String n = name.getText().trim();
            String ph = phone.getText().trim();
            String lic = license.getText().trim();

            if (n.isEmpty() || ph.isEmpty() || lic.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill all fields.");
                return;
            }

            for (Customer c : customers) {
                if (c.license.equalsIgnoreCase(lic)) {
                    JOptionPane.showMessageDialog(this,
                            "Licence already registered.");
                    return;
                }
            }

            Customer c = new Customer(nextCustomerId++, n, ph, lic);
            customers.add(c);

            show("Customer registered. ID: " + c.id);
            JOptionPane.showMessageDialog(this,
                    "Registration successful!\nCustomer ID: " + c.id
                    + "\nLicence verification is demo-only.");
            name.setText("");
            phone.setText("");
            license.setText("");
        });

        return p;
    }

    // MODULE 2: Vehicle Search
    JPanel vehiclePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.add(new JScrollPane(carTable), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh Vehicles");
        refresh.addActionListener(e -> refreshCars());
        p.add(refresh, BorderLayout.SOUTH);

        return p;
    }

    // MODULE 3: Booking and Price Calculation
    JPanel bookingPanel() {
        JPanel p = makePanel();

        addField(p, "Customer ID:", customerId, 0);
        addField(p, "Car ID:", carId, 1);
        addField(p, "Rental Days:", days, 2);

        JButton book = new JButton("Calculate and Book");
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = 3;
        g.insets = new Insets(8, 8, 8, 8);
        p.add(book, g);

        book.addActionListener(e -> {
            try {
                Customer c = findCustomer(number(customerId.getText()));
                Car car = findCar(number(carId.getText()));
                int d = number(days.getText());

                if (c == null || car == null || !car.available || d <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid customer, car, or rental days.");
                    return;
                }

                double base = car.price * d;
                double tax = base * 0.05;
                double deposit = 2000;
                double total = base + tax + deposit;

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Rental: Rs. " + base
                        + "\nDemo tax (5%): Rs. " + tax
                        + "\nSecurity deposit: Rs. " + deposit
                        + "\nTotal: Rs. " + total
                        + "\nConfirm booking?",
                        "Booking Details", JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) return;

                Booking b = new Booking(nextBookingId++, c, car, d, total);
                bookings.add(b);
                car.available = false;

                show("Booking created. Booking ID: " + b.id);
                JOptionPane.showMessageDialog(this,
                        "Booking successful!\nBooking ID: " + b.id
                        + "\nProceed to Payment tab.");

                refreshCars();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Enter valid numeric IDs and days.");
            }
        });

        return p;
    }

    // MODULE 4: Payment and Vehicle Handover
    JPanel paymentPanel() {
        JPanel p = makePanel();

        addField(p, "Booking ID:", paymentBookingId, 0);

        JButton pay = new JButton("Pay and Hand Over");
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = 1;
        g.insets = new Insets(8, 8, 8, 8);
        p.add(pay, g);

        pay.addActionListener(e -> {
            try {
                Booking b = findBooking(number(paymentBookingId.getText()));

                if (b == null || b.returned) {
                    JOptionPane.showMessageDialog(this,
                            "Booking not found or already completed.");
                    return;
                }

                if (!b.paid) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Pay Rs. " + b.amount + "?",
                            "Demo Payment", JOptionPane.YES_NO_OPTION);

                    if (confirm != JOptionPane.YES_OPTION) return;

                    b.paid = true;
                    show("Demo payment successful: " + b.id);
                }

                if (!b.handedOver) {
                    String fuel = JOptionPane.showInputDialog(
                            this, "Initial fuel level:");
                    String condition = JOptionPane.showInputDialog(
                            this, "Initial vehicle condition:");

                    if (fuel == null || condition == null
                            || fuel.trim().isEmpty()
                            || condition.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "Fuel and condition are required.");
                        return;
                    }

                    b.handedOver = true;
                    show("Vehicle handed over. Fuel: " + fuel
                            + ", Condition: " + condition);
                } else {
                    show("Vehicle already handed over.");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Enter a valid booking ID.");
            }
        });

        return p;
    }

    // MODULE 5: Rental Management
    JPanel rentalPanel() {
        JPanel p = makePanel();

        JTextField idField = new JTextField(8);
        addField(p, "Booking ID:", idField, 0);

        JButton manage = new JButton("Manage Rental");
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = 1;
        g.insets = new Insets(8, 8, 8, 8);
        p.add(manage, g);

        manage.addActionListener(e -> {
            try {
                Booking b = findBooking(number(idField.getText()));

                if (b == null || b.returned) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid or completed booking.");
                    return;
                }

                if (!b.handedOver) {
                    JOptionPane.showMessageDialog(this,
                            "Vehicle has not been handed over.");
                    return;
                }

                Object[] options = {
                    "Extend Rental", "Check Late Fee", "Cancel"
                };

                int choice = JOptionPane.showOptionDialog(this,
                        "Customer: " + b.customer.name
                        + "\nVehicle: " + b.car.name
                        + "\nBooked days: " + b.days,
                        "Rental Management",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null, options, options[0]);

                if (choice == 0) {
                    String input = JOptionPane.showInputDialog(
                            this, "Additional days:");
                    if (input == null) return;

                    int extra = number(input);
                    if (extra <= 0) throw new IllegalArgumentException();

                    double cost = b.car.price * extra * 1.05;

                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Extension cost: Rs. " + cost
                            + "\nConfirm demo payment?",
                            "Extension", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        b.days += extra;
                        b.amount += cost;
                        show("Rental extended. Booking: " + b.id
                                + ", New days: " + b.days);
                    }

                } else if (choice == 1) {
                    String input = JOptionPane.showInputDialog(
                            this, "Actual rental days so far:");
                    if (input == null) return;

                    int actual = number(input);
                    if (actual < 0) throw new IllegalArgumentException();

                    int late = Math.max(0, actual - b.days);
                    double fee = late * 500.0;

                    JOptionPane.showMessageDialog(this,
                            "Overdue days: " + late
                            + "\nLate fee: Rs. " + fee
                            + "\nDemo rate: Rs. 500/day");
                }

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        "Enter a valid positive number.");
            }
        });

        return p;
    }

    // MODULE 6: Vehicle Return and Settlement
    JPanel returnPanel() {
        JPanel p = makePanel();

        addField(p, "Booking ID:", returnBookingId, 0);
        addField(p, "Actual Rental Days:", actualDays, 1);

        JButton returnButton = new JButton("Return Vehicle");
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = 2;
        g.insets = new Insets(8, 8, 8, 8);
        p.add(returnButton, g);

        returnButton.addActionListener(e -> {
            try {
                Booking b = findBooking(number(returnBookingId.getText()));
                int actual = number(actualDays.getText());

                if (b == null || !b.handedOver || b.returned || actual <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid booking or rental days.");
                    return;
                }

                String condition = JOptionPane.showInputDialog(
                        this, "Return vehicle condition:");
                String fuel = JOptionPane.showInputDialog(
                        this, "Final fuel level:");

                if (condition == null || fuel == null
                        || condition.trim().isEmpty()
                        || fuel.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Return details are required.");
                    return;
                }

                double damage = 0, extraKm = 0;

                String damageAnswer = JOptionPane.showInputDialog(
                        this, "Damage charge (Rs. 0 if none):", "0");
                if (damageAnswer == null) return;
                damage = amount(damageAnswer);

                String kmAnswer = JOptionPane.showInputDialog(
                        this, "Extra kilometre charge (Rs. 0 if none):", "0");
                if (kmAnswer == null) return;
                extraKm = amount(kmAnswer);

                if (damage < 0 || extraKm < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Charges cannot be negative.");
                    return;
                }

                double lateFee = Math.max(0, actual - b.days) * 500.0;
                double charges = lateFee + damage + extraKm;
                double balance = charges - 2000;

                String settlement = balance > 0
                        ? "Additional payable: Rs. " + balance
                        : "Deposit refund: Rs. " + (-balance);

                JOptionPane.showMessageDialog(this,
                        "===== FINAL INVOICE ====="
                        + "\nBooking ID: " + b.id
                        + "\nCustomer: " + b.customer.name
                        + "\nVehicle: " + b.car.name
                        + "\nBooked days: " + b.days
                        + "\nActual days: " + actual
                        + "\nOriginal booking amount: Rs. " + b.amount
                        + "\nLate fee: Rs. " + lateFee
                        + "\nDamage charge: Rs. " + damage
                        + "\nExtra kilometre charge: Rs. " + extraKm
                        + "\nSecurity deposit: Rs. 2000"
                        + "\nReturn condition: " + condition
                        + "\nFinal fuel: " + fuel
                        + "\n" + settlement);

                b.returned = true;
                b.car.available = true;

                show("Return completed. Booking ID: " + b.id);
                refreshCars();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Enter valid numeric values.");
            }
        });

        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DriveEaseNew().setVisible(true);
        });
    }
}