//import javax.sound.midi.MidiEvent;
//import javax.sound.midi.ShortMessage;
//import java.util.LinkedList;
//import java.util.Queue;
//
//import static org.junit.Assert.*;
//
//public class RhythmGeneratorTest {
//    @org.junit.Test
//    public void generateMeasure() throws Exception {
//    }
//
//    @org.junit.Test
//    public void generateMeasure1() throws Exception {
//    }
//
//    @org.junit.Test
//    public void fillSequence() throws Exception {
//    }
//
//    @org.junit.Test
//    public void writeToMIDI() throws Exception {
//    }
//
//    @org.junit.Test
//    public void readMIDITest() throws Exception {
//        LinkedList<ShortMessage> noteOn = new LinkedList<>();
//        LinkedList<ShortMessage> noteOff = new LinkedList<>();
//        RhythmGenerator.readMIDI(noteOn, noteOff, "Parker,_Charlie_-_Donna_Lee.midi");
//        assertEquals(noteOn.size(), noteOff.size());
//    }
//
//    @org.junit.Test
//    public void getKeyTest() throws Exception {
//        LinkedList<ShortMessage> noteOn = new LinkedList<>();
//        LinkedList<ShortMessage> noteOff = new LinkedList<>();
//        RhythmGenerator.readMIDI(noteOn, noteOff, "Parker,_Charlie_-_Donna_Lee.midi");
//        int key = RhythmGenerator.getKey(noteOn);
//    }
//
//    @org.junit.Test
//    public void printMeasure() throws Exception {
//    }
//
//    @org.junit.Test
//    public void main() throws Exception {
//    }
//
//}