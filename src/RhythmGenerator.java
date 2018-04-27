import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.util.LinkedList;

public class RhythmGenerator {
    // beats[0]: Freddy Green
    // beats[1]: Charleston
    // beats[2]: Upbeat on 1 & 3
    // beats[3]: Upbeat on 2 & 4
    // beats[4]: Single chord per measure
    public static final int[][] beats = {
            {1, 0, 1, 0, 1, 0, 1, 0},
            {1, 0, 0, 1, 0, 0, 0, 0},
            {0, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0}
    };

    public static final int[][] beatLengths = {
            {4, 0, 4, 0, 4, 0, 4, 0},
            {3, 0, 0, 8, 0, 0, 0, 0},
            {0, 8, 0, 0, 0, 8, 0, 0},
            {0, 0, 0, 8, 0, 0, 0, 8},
            {1, 0, 0, 0, 0, 0, 0, 0}
    };

    public static final int noteRes = 24;

    /**
     * Generate a single measure of backing rhythm
     * @param chord
     * @param beatType The style of the rhythm
     * @return A measure with the implemented rhythm style and note
     */
    public static Chord[] generateMeasure(Chord chord, int beatType) {
        int[] beat = beats[beatType];
        Chord[] measure = new Chord[beat.length];
        for (int i = 0; i < measure.length; i++) {
            if (beat[i] != 0) {
                measure[i] = chord;
                measure[i].setLength(beatLengths[beatType][i]);
            } else {
                measure[i] = null;
            }
        }
        return measure;
    }

    /**
     * Generate a single measure of backing rhythm using custom parameters
     * @param chord
     * @param beat the custom beat to be played
     * @param lengths lengths of the beat to be played; must be the same length of
     *               the beat array, and each entry must correspond to an entry in beat
     * @return A measure with the implemented rhythm style and note
     */
    public static Chord[] generateMeasure(Chord chord, int[] beat, int[] lengths) {
        Chord[] measure = new Chord[beat.length];
        for (int i = 0; i < measure.length; i++) {
            if (beat[i] != 0) {
                if (lengths[i] == 0) {
                    throw new IllegalArgumentException("Beats and Lengths must match up");
                }
                measure[i] = chord;
                measure[i].setLength(lengths[i]);
            } else {
                measure[i] = null;
            }
        }
        return measure;
    }

    /**
     * Generates a rhythm pattern for a measure based on current, previous, next measures
     * @param prevMeasure the measure previous to the current measure, can be null if current measure is the first measure
     * @param measure the current measure to write a rhythm to, cannot be null
     * @param nextMeasure the measure after the current measure, can be null if the current measure is the last measure
     * @return a 2D array with two rows; the first row is the rhythm pattern of the measure, and the second row is the beat length
     */
    public static int[][] generateRhythm(int[] prevMeasure, int[] measure, int[] nextMeasure) {
        int[][] rhythm = new int[2][8];
        boolean emptySpace = false;
        int emptyLength = 0;
        int firstChord = 0;
        for (int i = 0; i < measure.length; i++) {
            if (measure[i] == 0) {
                emptySpace = true;
                emptyLength++;
            } else if (emptySpace) {
                emptySpace = false;
                if (emptyLength > (measure.length / 8) * 2) {
                    rhythm[0][(i / (measure.length / 8)) - 1] = 1;
                    rhythm[1][(i / (measure.length / 8)) - 1] = 8;
                    firstChord = firstChord == 0 ? (i / (measure.length / 8)) - 1 : firstChord;
                }
                emptyLength = 0;
            }
        }
        rhythm[0][0] = 1;
            rhythm[1][0] = firstChord < 2 ? 8 : 4;
        return rhythm;
    }

    /**
     * Divides a MIDI sequence into separate measures
     * @param s the sequence to be divided
     * @return an array of measures filled with each of their respective notes
     */
    public static int[][] divideSequence(Sequence s){
        LinkedList<MidiEvent> noteOn = new LinkedList<>();
        LinkedList<MidiEvent> noteOff = new LinkedList<>();
        readMIDI(noteOn, noteOff, s);
        int length = (int) s.getTickLength() / s.getResolution();

        int[][] measures = new int[length / 4 + 1][noteRes];
        for (int m = 0; m < measures.length; m++) {
            for (int n = 0; n < measures[0].length; n++) {
                while (!noteOn.isEmpty() &&
                        (int) noteOn.getFirst().getTick() < (n + 1 + m * noteRes) * (int) s.getResolution() / (noteRes / 4) ) {
                    MidiEvent me = noteOn.remove();
                    if (me.getMessage() instanceof ShortMessage) {
                        ShortMessage sm = (ShortMessage) me.getMessage();
                        measures[m][n] = sm.getData1();
                    }
                }
            }
        }
        return measures;
    }

    /**
     * Creates a backing rhythm for a MIDI track
     * @param t The track for which the backing rhythm is to be played
     * @param ppq the resolution of the sequence in pulses per quarter note
     * @param rootNote The root note of the chord progression
     * @return A Chord array representing the chord progression of the backing rhythm
     */
    public static Chord[] fillSequence(Track t, int ppq, int rootNote) {
        int trackLength = ((int) t.ticks()) / ppq;
        int numMeasures = trackLength / 4;
        Chord[] sequence = new Chord[8 * numMeasures];
        for (int i = 0; i < numMeasures; i++) {
            Chord[] measure = new Chord[8];
            int keySelect = i % 4;
            switch (keySelect) {
                case 0:
                    measure = generateMeasure(new Chord(rootNote + 2, "m"), 2);
                    break;
                case 1:
                    measure = generateMeasure(new Chord(rootNote + 7), 2);
                    break;
                case 2:
                case 3:
                    measure = generateMeasure(new Chord(rootNote), 2);
                    break;
            }
            for (int j = 0; j < measure.length; j++) {
                sequence[i*8 + j] = measure[j];
            }
        }
        return sequence;
    }

    /**
     * Creates a backing rhythm for a MIDI track
     * @param t The track for which the backing rhythm is to be played
     * @param ppq the resolution of the sequence in pulses per quarter note
     * @param rootNote The root note of the chord progression
     * @param rhythm The array of rhythms and beats
     * @return A Chord array representing the chord progression of the backing rhythm
     */
    public static Chord[] fillSequence(Track t, int ppq, int rootNote, int[][][] rhythm) {
        int trackLength = ((int) t.ticks()) / ppq;
        int numMeasures = trackLength / 4;
        Chord[] sequence = new Chord[8 * numMeasures];
        for (int i = 0; i < numMeasures; i++) {
            Chord[] measure = new Chord[8];
            int keySelect = i % 4;
            switch (keySelect) {
                case 0:
                    measure = generateMeasure(new Chord(rootNote + 2, "m"), rhythm[i][0], rhythm[i][1]);
                    break;
                case 1:
                    measure = generateMeasure(new Chord(rootNote + 7), rhythm[i][0], rhythm[i][1]);
                    break;
                case 2:
                case 3:
                    measure = generateMeasure(new Chord(rootNote), rhythm[i][0], rhythm[i][1]);
                    break;
            }
            for (int j = 0; j < measure.length; j++) {
                sequence[i*8 + j] = measure[j];
            }
        }
        return sequence;
    }

    /**
     * Writes the chord sequence to a MIDI file
     * @param sequence the sequence to be written
     * @param ppq the resolution of the sequence in pules per quarter note
     * @param fileName the name of the file to be written
     */
    public static void writeToMIDI(Chord[] sequence, int ppq, String fileName) {
        try {
            Sequence s = new Sequence(Sequence.PPQ, ppq);
            Track t = s.createTrack();

            //****  General MIDI sysex -- turn on General MIDI sound set  ****
            byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
            SysexMessage sm = new SysexMessage();
            sm.setMessage(b, 6);
            MidiEvent me = new MidiEvent(sm,(long)0);
            t.add(me);

            //****  set track name (meta event)  ****
            MetaMessage mt = new MetaMessage();
            String TrackName = "midifile track";
            mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
            me = new MidiEvent(mt,(long)0);
            t.add(me);

            //****  set omni on  ****
            ShortMessage mm = new ShortMessage();
            mm.setMessage(0xB0, 0x7D,0x00);
            me = new MidiEvent(mm,(long)0);
            t.add(me);

            //****  set poly on  ****
            mm = new ShortMessage();
            mm.setMessage(0xB0, 0x7F,0x00);
            me = new MidiEvent(mm,(long)0);
            t.add(me);

            //****  set instrument to Piano  ****
            mm = new ShortMessage();
            mm.setMessage(0xC0, 0x00, 0x00);
            me = new MidiEvent(mm,(long)0);
            t.add(me);

            for (int i = 0; i < sequence.length; i++) {
                if (sequence[i] != null) {
                    for (int note : sequence[i].getNotes()) {
                        mm = new ShortMessage();
                        mm.setMessage(0x90, note, 94);
                        me = new MidiEvent(mm, ((long) i * (ppq/2) + 1));
                        t.add(me);
                        mm = new ShortMessage();
                        mm.setMessage(0x80, note, 94);
                        me = new MidiEvent(mm, ((long) i * (ppq/2)
                                + (ppq*4/sequence[i].getLength()) + 1));
                        t.add(me);
                    }
                }
            }

            //****  set end of track (meta event) 19 ticks later  ****
            mt = new MetaMessage();
            byte[] bet = {}; // empty array
            mt.setMessage(0x2F,bet,0);
            me = new MidiEvent(mt, ((long) (sequence.length + 8)* ppq));
            t.add(me);

            //****  write the MIDI sequence to a MIDI file  ****
            File f = new File(fileName);
            MidiSystem.write(s,1,f);

        } catch (Exception e) {
            System.out.println("Exception caught: " + e.getMessage());
        }
    }

    /**
     *  Writes a backing rhythm onto a solo
     * @param fileName the name of the file of the solo
     * @return a MIDI sequence of the solo and backing rhythm combined
     * @throws Exception if the file is invalid
     */
    public static Sequence writeOnSolo(String fileName) throws Exception {
        Sequence sequence = MidiSystem.getSequence(new File(fileName));
        int ppq = 0;
        if (sequence.getDivisionType() == 0.0f) {
            ppq = sequence.getResolution();
        } else {
            throw new IllegalArgumentException("Cannot handle divisionTypes that are not PPQ");
        }
        int[][] measures = divideSequence(sequence);
        int[][][] rhythm = new int[measures.length][2][measures[0].length];
        for (int i = 0; i < measures.length; i++) {
            if (i == 0) {
                rhythm[i] = generateRhythm(null, measures[i], measures[i + 1]);
            } else if (i + 1 == measures.length) {
                rhythm[i] = generateRhythm(measures[i - 1], measures [i], null);
            } else {
                rhythm[i] = generateRhythm(measures[i - 1], measures[i], measures[i + 1]);
            }
        }
        LinkedList<MidiEvent> noteOn = new LinkedList<>();
        LinkedList<MidiEvent> noteOff = new LinkedList<>();
        readMIDI(noteOn, noteOff, sequence);
        int key = getKey(noteOn);
        int trackNumber = 0;
        for (Track t : sequence.getTracks()) {
            trackNumber++;
            Chord[] progression = fillSequence(t, ppq, key,rhythm);
            for (int i = 0; i < progression.length; i++) {
                if (progression[i] != null) {
                    for (int note : progression[i].getNotes()) {
                        ShortMessage mm = new ShortMessage();
                        mm.setMessage(0x90, note, 60);
                        MidiEvent me = new MidiEvent(mm, ((long) i * (ppq/2) + 1));
                        t.add(me);
                        mm = new ShortMessage();
                        mm.setMessage(0x80, note, 60);
                        me = new MidiEvent(mm, ((long) i * (ppq/2)
                                + (ppq*4/progression[i].getLength()) + 1));
                        t.add(me);
                    }
                }
            }
        }
        return sequence;
    }

    /**
     * Reads a MIDI sequence and writes its NOTE_ON and NOTE_OFF events to lists
     * @param noteOn the list of NOTE_ON events
     * @param noteOff the list of NOTE_OFF events
     * @param s the sequence to be read
     * @throws Exception if the file is invalid
     */
    public static void readMIDI(LinkedList<MidiEvent> noteOn, LinkedList<MidiEvent> noteOff,
                                Sequence s) {
        //Sequence sequence = MidiSystem.getSequence(new File(fileName));
        for (Track t : s.getTracks()) {
            for (int i = 0; i < t.size(); i++) {
                MidiEvent event = t.get(i);
                if (event.getMessage() instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) event.getMessage();
                    if (sm.getCommand() == 0x90) {
                        if (sm.getData2() != 0) {
                            noteOn.add(event);
                        } else {
                            noteOff.add(event);
                        }
                    } else if (sm.getCommand() == 0x80){
                        noteOff.add(event);
                    }
                }
            }
        }
    }

    /**
     * A primitive method of finding the key
     * @param noteOn the list of NOTE_ON events
     * @return the key
     */
    public static int getKey(LinkedList<MidiEvent> noteOn) {
        if (noteOn.getLast().getMessage() instanceof ShortMessage) {
            ShortMessage lastNote = (ShortMessage) noteOn.getLast().getMessage();
            return lastNote.getData1();
        } else throw new IllegalArgumentException("Input list must contain only ShortMessages");
    }

    /**
     * A debugging tool for viewing a measure on the console
     * @param measure the measure to be printed
     */
    private static void printMeasure(Chord[] measure) {
        for (Chord chord : measure) {
            if (chord == null) {
                System.out.print("-");
            } else {
                System.out.print(chord.getChordName());
            }
        }
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        try {
            Sequence sequence = writeOnSolo("Parker,_Charlie_-_Donna_Lee.midi");
            File f = new File("compSolo2.mid");
            MidiSystem.write(sequence,1,f);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
//        Sequence sequence = MidiSystem.getSequence(new File("Parker,_Charlie_-_Donna_Lee.midi"));
//        int[][] measures = divideSequence(sequence);
//        int[][][] rhythm = new int[measures.length][2][measures[0].length];
//        for (int i = 0; i < measures.length; i++) {
//            if (i == 0) {
//                rhythm[i] = generateRhythm(null, measures[i], measures[i + 1]);
//            } else if (i + 1 == measures.length) {
//                rhythm[i] = generateRhythm(measures[i - 1], measures [i], null);
//            } else {
//                rhythm[i] = generateRhythm(measures[i - 1], measures[i], measures[i + 1]);
//            }
//        }


//        int ppq = 0;
//        if (sequence.getDivisionType() == 0.0f) {
//            ppq = sequence.getResolution();
//        }
//        int trackNumber = 0;
//        for (Track t : sequence.getTracks()) {
//            trackNumber++;
//            Chord[] progression = fillSequence(t, ppq, 68);
//            writeToMIDI(progression, ppq, String.format("ChordProgression%d.mid", trackNumber));
//        }
//        Chord Ab = new Chord(80, "sus4");
//        Chord[] freddyGreen = generateMeasure(Ab, 0);
//        Chord[] charleston = generateMeasure(Ab, 1);
//        Chord[] up13 = generateMeasure(Ab, 2);
//        Chord[] up24 = generateMeasure(Ab, 3);
//        System.out.println("Freddy Green:");
//        printMeasure(freddyGreen);
//        System.out.println("Charleston:");
//        printMeasure(charleston);
//        System.out.println("Upbeat on 1 & 3:");
//        printMeasure(up13);
//        System.out.println("Upbeat on 2 & 4:");
//        printMeasure(up24);
//        writeToMIDI(freddyGreen, 960, "FreddyGreen.mid");
//        writeToMIDI(charleston, 960, "Charleston.mid");
//        writeToMIDI(up13, 960, "up13.mid");
//        writeToMIDI(up24, 960, "up24.mid");
//        Queue<MidiEvent> noteOn = new LinkedList<>();
//        Queue<MidiEvent> noteOff = new LinkedList<>();
//        try {
//            readMIDI(noteOn, noteOff);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
    }
}