package submit.goop.house.data.entity;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Lob;
import submit.goop.house.data.AbstractEntity;

@Entity
public class Submission extends AbstractEntity {

    @Lob
    private String coverArt;
    @Lob
    private String audioFileURL;
    private UUID submissionID;
    private String mainArtist;
    private String title;
    private String event;

    public String getCoverArt() {
        return coverArt;
    }
    public void setCoverArt(String coverArt) {
        this.coverArt = coverArt;
    }
    public String getAudioFileURL() {
        return audioFileURL;
    }
    public void setAudioFileURL(String audioFileURL) {
        this.audioFileURL = audioFileURL;
    }
    public UUID getSubmissionID() {
        return submissionID;
    }
    public void setSubmissionID(UUID submissionID) {
        this.submissionID = submissionID;
    }
    public String getMainArtist() {
        return mainArtist;
    }
    public void setMainArtist(String mainArtist) {
        this.mainArtist = mainArtist;
    }
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public void setEvent(String event) {this.event = event;}
    public String getEvent() {return event;}
}
