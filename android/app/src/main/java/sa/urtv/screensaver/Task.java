package sa.urtv.screensaver;

/** One task kept on the TV. The id is its own and stays with it (0: a task saved before ids existed, given one when the list is read). */
final class Task {
    final String text;
    final boolean done;
    final long at;
    final long id;

    Task(String text, boolean done, long at, long id) {
        this.text = text;
        this.done = done;
        this.at = at;
        this.id = id;
    }
}
