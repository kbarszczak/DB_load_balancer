package main;

import jakarta.persistence.PersistenceException;
import loadbalancer.loadbalancingmechanism.RandConnection;
import loadbalancer.loadbalancingmechanism.RoundRobin;
import logging.DBLogger;
import model.User;
import org.hibernate.Session;
import org.hibernate.query.Query;
import loadbalancer.HibernateLoadBalancer;
import loadbalancer.LoadBalancer;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        Logger.getLogger("org.hibernate").setLevel(Level.OFF);
        try (LoadBalancer<Session> loadBalancer = new HibernateLoadBalancer(List.of(
                "/hibernate/hibernate-psql-1.cfg.xml",
                "/hibernate/hibernate-psql-2.cfg.xml",
                "/hibernate/hibernate-psql-3.cfg.xml",
                "/hibernate/hibernate-psql-4.cfg.xml"
        ))) {
            boolean run = true;
            boolean logging = true;
            while (run) {
                try {
                    int choice = action("Exit", "Add user", "Add random user", "List all users", "List all users with id in range", "Update user", "Delete user", "Turn off/on logging", "Change load balancing mechanism");

                    switch (choice) {
                        case 0 -> run = false;
                        case 1 -> {
                            String name;
                            System.out.print("User name: ");
                            name = SCANNER.nextLine();

                            User user = new User(name);
                            Session connection = loadBalancer.connection();
                            connection.beginTransaction();
                            connection.persist(user);
                            connection.getTransaction().commit();
                            if (logging) DBLogger.getLogger(Main.class).info("User successfully saved to db");
                        }
                        case 2 -> {
                            User user = new User("Kamil" + RANDOM.nextInt());
                            Session connection = loadBalancer.connection();
                            connection.beginTransaction();
                            connection.persist(user);
                            connection.getTransaction().commit();
                            if (logging) DBLogger.getLogger(Main.class).info("User successfully saved to db");
                        }
                        case 3 -> {
                            Session connection = loadBalancer.connection();
                            Query<User> query = connection.createQuery("from User", User.class);
                            System.out.println("Loaded users:");
                            for (Object user : query.list()) {
                                System.out.println(user);
                            }
                        }
                        case 4 -> {
                            int smaller, bigger;
                            System.out.print("Smaller id: ");
                            smaller = Integer.parseInt(SCANNER.nextLine());
                            System.out.print("Bigger id: ");
                            bigger = Integer.parseInt(SCANNER.nextLine());

                            if (bigger <= smaller)
                                throw new IllegalArgumentException("Value '" + bigger + "' is not bigger than '" + smaller + "'");

                            Session connection = loadBalancer.connection();
                            Query<User> query = connection.createQuery("from User where id >= ?1 and id <= ?2", User.class);
                            query.setParameter(1, smaller);
                            query.setParameter(2, bigger);
                            System.out.println("Loaded users:");
                            for (Object user : query.list()) {
                                System.out.println(user);
                            }
                        }
                        case 5 -> {
                            long id;
                            String newName;
                            System.out.print("Id of the user: ");
                            id = Long.parseLong(SCANNER.nextLine());

                            Session connection = loadBalancer.connection();
                            Query<User> query = connection.createQuery("from User where id = ?1", User.class);
                            query.setParameter(1, id);
                            User user = query.uniqueResult();
                            if (user == null)
                                throw new IllegalArgumentException("User with id '" + id + "' does not exists");

                            System.out.print("New name: ");
                            newName = SCANNER.nextLine();

                            connection.beginTransaction();
                            user.setName(newName);
                            connection.merge(user);
                            connection.getTransaction().commit();

                            if (logging) DBLogger.getLogger(Main.class).info("User successfully updated");
                        }
                        case 6 -> {
                            long id;
                            System.out.print("Id of the user: ");
                            id = Long.parseLong(SCANNER.nextLine());

                            Session connection = loadBalancer.connection();
                            Query<User> query = connection.createQuery("from User where id = ?1", User.class);
                            query.setParameter(1, id);
                            User user = query.uniqueResult();
                            if (user == null)
                                throw new IllegalArgumentException("User with id '" + id + "' does not exists");

                            connection.beginTransaction();
                            connection.remove(user);
                            connection.getTransaction().commit();

                            if (logging) DBLogger.getLogger(Main.class).info("User successfully removed");
                        }
                        case 7 -> {
                            logging = !logging;
                            loadBalancer.setLogging(logging);
                            DBLogger.getLogger(Main.class).info("Logging mode set to: " + (logging ? "true" : "false"));
                        }
                        case 8 -> {
                            choice = action("RoundRobin", "RandomConnection");
                            loadBalancer.setLoadBalancingMechanism(
                                    switch (choice){
                                        case 0 -> new RoundRobin<>();
                                        case 1 -> new RandConnection<>();
                                        default -> throw new IllegalArgumentException("Unknown option");
                                    }
                            );
                        }
                        default -> throw new IllegalArgumentException("Invalid option");
                    }
                } catch (PersistenceException | IllegalArgumentException | IllegalStateException exception) {
                    if (logging)
                        DBLogger.getLogger(Main.class).warning("The following exception occurred: " + exception.getMessage());
                }
            }
        } catch (Exception exception) {
            DBLogger.getLogger(Main.class).severe("The following critical exception occurred: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static int action(String... options) throws IllegalArgumentException {
        int i = 0;
        System.out.println("Menu");
        for (String option : options) {
            System.out.printf("%d: %s\n", i++, option);
        }

        boolean ok;
        do {
            try {
                i = Integer.parseInt(SCANNER.nextLine());
                if (i < 0 || i >= options.length) throw new IllegalArgumentException("Index out of range");
                ok = true;
            } catch (IllegalArgumentException exception) {
                System.err.println(exception.getMessage());
                ok = false;
            }
        } while (!ok);

        return i;
    }

}