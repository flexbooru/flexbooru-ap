package onlymash.flexbooru.ap.data

enum class Status {
    /**
     * There is current a running request.
     */
    RUNNING,

    /**
     * The last request has succeeded or no such requests have ever been run.
     */
    SUCCESS,

    /**
     * The last request has failed.
     */
    FAILED
}