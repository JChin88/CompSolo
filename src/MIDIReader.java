import java.io.File;
import java.util.Queue;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MIDIReader {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private String fileName;

    public MIDIReader(String fileName) {
        this.fileName = fileName;
    }

    public MIDIReader() {
        this("Parker,_Charlie_-_Donna_Lee.midi");
    }

    public static void readMIDI(Queue<MidiEvent> noteOn, Queue<MidiEvent> noteOff) throws Exception {
        Sequence sequence = MidiSystem.getSequence(new File("Parker,_Charlie_-_Donna_Lee.midi"));
        Track t = sequence.createTrack();
        for (int i = 0; i < t.size(); i++) {
            MidiEvent event = t.get(i);
            if (event.getMessage() instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) event.getMessage();
                if (sm.getCommand() == 0x90 && sm.getData2() != 0) {
                    noteOn.add(event);
                } else {
                    noteOff.add(event);
                }
            }
        }
    }
}
