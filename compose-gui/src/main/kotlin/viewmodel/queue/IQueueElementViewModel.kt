package viewmodel.queue

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import viewmodel.queue.IQueueElementViewModel.Status.*

sealed interface IQueueElementViewModel {
    /**
     * Values:
     *
     * [INITIALIZING] - Element is initializing
     *
     * [READY_TO_USE] - Element initialized and waits a new operation
     *
     * [IN_USE] - Element is downloading something
     *
     * [ERROR] - Shows that element operation has failed. Usually set with [an error message][IQueueElementViewModel.lastErrorMessage]
     *
     * [SAVED] - Element finished downloading successfully with no errors
     */
    enum class Status {
        /**
         * Element is initializing
         */
        INITIALIZING,

        /**
         * Element initialized and waits a new operation
         */
        READY_TO_USE,

        /**
         * Element is downloading something
         */
        IN_USE,

        /**
         * Shows that element operation has failed. Usually set with [an error message][IQueueElementViewModel.lastErrorMessage]
         */
        ERROR,

        /**
         * Element finished downloading successfully with no errors
         */
        SAVED
    }

    /**
     * Current status of element. [Values][Status]
     */
    val status: StateFlow<Status>

    /**
     * Shows last error message of element
     */
    val lastErrorMessage: StateFlow<String?>

    /**
     * Shows last progress of queue element
     */
    val progress: StateFlow<String?>

    /**
     * Current job of queue element
     */
    val currentJob: StateFlow<Job?>

    /**
     * Path to folder where element is saved
     */
    val savedTo: StateFlow<String?>

    /**
     * Start initializing operation async.
     *
     * If returned value is null, it means that QueueElement is busy with [currentJob] and
     * your request was ignored
     *
     * If queue element isn't busy, you'll get Deferred nullable throwable, showing how operation ended.
     * If throwable is null, then operation finished successfully
     */
    fun initializeAsync(): Deferred<Throwable?>?

    /**
     * Start saving operation async.
     *
     * If returned value is null, it means that QueueElement is busy with [currentJob] and
     * your request was ignored
     *
     * If queue element isn't busy, you'll get Deferred nullable throwable, showing how operation ended.
     * If throwable is null, then operation finished successfully
     */
    fun saveAsync(): Deferred<Throwable?>?
}