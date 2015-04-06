package gr.istl.soccer;



import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author gaitanesnikos
 */
public class Sounds implements Runnable {// gia na valw ixo an thelw sto podosfairaki m

    private String songName;
    private AudioInputStream audio = null;
    boolean resume = false;
    private Clip clip;

    public Sounds(String songName) {
        try {
            if (songName != null) {
                this.songName = songName;


                clip = AudioSystem.getClip();
                audio = AudioSystem.getAudioInputStream(new File(songName));

                clip.open(audio);
            }
        } catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();

        } catch (Exception ex) {
            ex.printStackTrace();

        }
//////// File f = new File(song + ".au");
////////            clip = Applet.newAudioClip(f.toURL());
////////            
////////            clip.play();
////////            
////////        } catch (Exception e) {
////////            e.printStackTrace();
////////        } catch (OutOfMemoryError e) {
////////            System.out.println("try again");
////////        
////////         
////////            
////////        }


    }
/**
 * 
 */
    @Override
    public void run() {
        if (songName != null) {
            clip.start();
        }

    }
/**
 * 
 */
    public void stopClip() {
        if (songName != null) {
            clip.stop();
        }

    }
/**
 * 
 * @return 
 */
    public String getSongName() {
        return songName;
    }
/**
 * 
 * @param songName 
 */
    public void setSongName(String songName) {
        this.songName = songName;
        try {
            audio = AudioSystem.getAudioInputStream(new File(songName));
        } catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
         ex.printStackTrace();
        }
    }
}
