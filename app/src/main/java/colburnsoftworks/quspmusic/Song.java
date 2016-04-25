package colburnsoftworks.quspmusic;

/**
 * Created by dalex on 12/30/2015.
 */
public class Song {

    // Variable stored for each track
    private long id;
    private String title;
    private String artist;
    private String format;

    public Song(long songID, String songTitle, String songArtist, String songFormat){
        id=songID;
        title=songTitle;
        artist=songArtist;
        format=songFormat;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getFormat(){return format;}
}
