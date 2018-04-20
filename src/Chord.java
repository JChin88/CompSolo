public class Chord {

    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private int key;
    private int[] notes;
    private String chordName;
    private int length;

    //generate custom chord
    public Chord(int[] notes) {
        if (notes.length <= 0) {
            throw new IllegalArgumentException("Input must contain at least 1 note");
        }
        this.length = 1;
        this.notes = notes;
        key = notes[0];
        chordName = NOTE_NAMES[key % 12];
    }

    //generate major chord
    public Chord(int key) {
        this.length = 1;
        this.key = key;
        notes = new int[3];
        notes[0] = key;
        notes[1] = key + 4;
        notes[2] = key + 7;
        chordName = NOTE_NAMES[key % 12];
    }

    public Chord(int key, String modifier) {
        this(key);
        switch (modifier) {
            case "m":
                notes[1] = key + 3;
                break;
            case "dim":
                notes[1] = key + 3;
                notes[2] = key + 6;
                break;
            case "sus2":
                notes[1] = key + 2;
                break;
            case "sus4":
                notes[1] = key + 5;
                break;
            default:
                throw new IllegalArgumentException("Need a valid modifier");
        }
    }

    public int getKey() {
        return key;
    }

    public int[] getNotes() {
        return notes;
    }

    public String getChordName() {
        return chordName;
    }

    public int getLength() {
        return length;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setNotes(int[] notes) {
        this.notes = notes;
    }

    public void setChordName(String chordName) {
        this.chordName = chordName;
    }

    public void setLength(int length) {
        this.length = length;
    }
}

