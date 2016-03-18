package sg.com.kaplan.pdma.currencyconverter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class about extends AppCompatActivity {

    // about messages
    private static final String str_about[] = {
            "<b>Currency Converter</b>",
            "",
            "Create for PDMA Assignment No.2",
            "sg.com.kaplan.pdma",
            "",
            "Credits:",
            "National flag image source:",
            "Sodipodi Flag collection. Available at http://www.sourceforge.net/projects/sodipodi/files/sodipodi-clipart/flags-1.6/",
            "",
            "Application icon source:",
            "IconEden Free Icons. Available at http://www.iconeden.com/",
            "",
            "History:",
            "v1.0    initial release"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        TextView about_text = (TextView) this.findViewById(R.id.content);

        // show about message
        String html_about = "";
        for (int i = 0; i < str_about.length; i++) {
            html_about = html_about + str_about[i] + "<br />";
        }

        about_text.setText(android.text.Html.fromHtml(html_about));
    }
}
