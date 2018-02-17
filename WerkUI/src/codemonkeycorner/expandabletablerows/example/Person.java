package codemonkeycorner.expandabletablerows.example;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Person {
    private final SimpleStringProperty firstName;
    private final SimpleStringProperty lastName;
    private final SimpleStringProperty email;
    private final SimpleStringProperty bio;
    private SimpleBooleanProperty expanded;

    public Person(String fName, String lName, String email, String bio) {
        this.firstName = new SimpleStringProperty(fName);
        this.lastName = new SimpleStringProperty(lName);
        this.email = new SimpleStringProperty(email);
        this.bio = new SimpleStringProperty(bio);
        this.expanded = new SimpleBooleanProperty(false);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty bioProperty() {
        return bio;
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String fName) {
        firstName.set(fName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String fName) {
        lastName.set(fName);
    }

    public String getBio() {
        return bio.get();
    }

    public void setBio(String pBio) {
        bio.set(pBio);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String fName) {
        email.set(fName);
    }

    public boolean isExpanded() {
        return expanded.get();
    }

    public void setExpanded(boolean isExpanded) {
        expanded.set(isExpanded);
    }

    @Override
    public String toString() {
        return String.format("%s %s", getFirstName(), getLastName());
    }
}
