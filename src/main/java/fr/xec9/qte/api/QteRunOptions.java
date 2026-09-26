package fr.xec9.qte.api;

/** Controls side effects applied when a QTE completes. */
public record QteRunOptions(boolean executeConfiguredCommands) {
    /** Preserves the behavior of /qte play and existing Java callers. */
    public static final QteRunOptions DEFAULT = new QteRunOptions(true);

    /** Integration mode: report the result without running definition result commands. */
    public static final QteRunOptions INTEGRATION = new QteRunOptions(false);
}
