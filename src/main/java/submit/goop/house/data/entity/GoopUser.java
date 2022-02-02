package submit.goop.house.data.entity;

import java.util.UUID;
import javax.persistence.Entity;
import submit.goop.house.data.AbstractEntity;

@Entity
public class GoopUser extends AbstractEntity {

    private UUID discordID;
    private String artistName;
    private String pronouns;
    private String email;
    private String phone;
    private String submissions;
    private boolean activeSubmission;

    public UUID getDiscordID() {
        return discordID;
    }
    public void setDiscordID(UUID discordID) {
        this.discordID = discordID;
    }
    public String getArtistName() {
        return artistName;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
    public String getPronouns() {
        return pronouns;
    }
    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getSubmissions() {
        return submissions;
    }
    public void setSubmissions(String submissions) {
        this.submissions = submissions;
    }
    public boolean isActiveSubmission() {
        return activeSubmission;
    }
    public void setActiveSubmission(boolean activeSubmission) {
        this.activeSubmission = activeSubmission;
    }

}
