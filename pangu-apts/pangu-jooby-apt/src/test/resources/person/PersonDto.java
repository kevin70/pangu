package person;

public class PersonDto {

    /**
     * 首名字.
     */
    private String firstname;
    /**
     * 尾名字.
     */
    private String lastname;
    /**
     * 性别.
     */
    private boolean gender;
    /**
     * 身高.
     */
    private int height;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
