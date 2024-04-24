package plu.capstone.playerpiano.addon.minecraftmod;

public class Scroller {

    private final int limit;
    private String msg;

    private int index = 0;

    public Scroller() {
        this.msg = "...";
        this.limit = 16;
    }

    public Scroller(String msg, int limit) {
        this.msg = msg;
        this.limit = limit;
    }

    public void setMsg(String msg) {
        if(msg == null) throw new IllegalArgumentException("Message cannot be null");
        if(msg.length() > limit) {
            msg = msg + "     ";
        }
        this.msg = msg;
    }

    public String next() {

        // If the message is short enough, return it
        if(msg.length() <= limit) return msg;

        // Get the substring to display
        String substring;
        if (index + limit <= msg.length()) {
            // If there's enough characters ahead to display
            substring = msg.substring(index, index + limit);
        } else {
            // If not enough characters ahead, wrap around
            substring = msg.substring(index) + msg.substring(0, limit - (msg.length() - index));
        }

        // Update the index for the next call
        index = (index + 1) % msg.length();

        return substring;

    }

    public String getOrigMsg() {
        return msg;
    }
}
