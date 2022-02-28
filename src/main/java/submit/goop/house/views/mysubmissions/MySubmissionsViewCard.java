package submit.goop.house.views.mysubmissions;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;

@JsModule("./views/mysubmissions/my-submissions-view-card.ts")
@Tag("my-submissions-view-card")
public class MySubmissionsViewCard extends LitTemplate {

    @Id
    private Image image;

    @Id
    private Span header;

    @Id
    private Span subtitle;

    @Id
    private Paragraph text;

    @Id
    private Span badge;

    public MySubmissionsViewCard(String songName, String event, String imageUrl, String artists) {
        this.image.setSrc(imageUrl);
        //this.image.setAlt(text);
        this.header.setText(songName);
        this.subtitle.setText(event);
        this.text.setText(
                "By " + artists);
        this.badge.setText("Certified Goopy");
    }
}
