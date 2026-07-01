package com.project.voicetotask.presentation.export

import com.project.voicetotask.domain.model.Meeting
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.presentation.screens.history.MeetingDetailUiState
import com.project.voicetotask.presentation.screens.task.TaskModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportFormatter {
    private const val BACKUP_FORMAT_VERSION = 1
    private const val ROOM_SCHEMA_VERSION = 4

    private val fileDateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    private val readableDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun timestampForFile(now: Long = System.currentTimeMillis()): String {
        return fileDateFormatter.format(Date(now))
    }

    fun meetingTranscriptText(state: MeetingDetailUiState): String {
        return buildString {
            appendLine(state.title.ifBlank { "Meeting transcript" })
            appendLine("Date: ${state.date.ifBlank { "--" }}")
            appendLine("Duration: ${state.duration.ifBlank { "--:--" }}")
            appendLine()
            appendLine("Transcript")
            appendLine(state.transcript.ifBlank { "Transcript is unavailable." })
        }.trim()
    }

    fun meetingNotesText(state: MeetingDetailUiState): String {
        return buildString {
            appendLine(state.title.ifBlank { "Meeting notes" })
            appendLine("Date: ${state.date.ifBlank { "--" }}")
            appendLine("Duration: ${state.duration.ifBlank { "--:--" }}")
            appendLine()
            appendSection("Summary", state.summary)
            appendSection("Decisions", state.decisionsText)
            appendSection("Blockers/Risks", state.blockersText)
            appendSection("Follow-ups", state.followUpsText)
            appendTasks(state.tasks)
            appendSection("Transcript", state.transcript)
        }.trim()
    }

    fun linkedTasksCsv(tasks: List<TaskModel>): String {
        return tasksCsv(tasks)
    }

    fun visibleTasksCsv(tasks: List<TaskModel>): String {
        return tasksCsv(tasks)
    }

    fun backupJson(
        meetings: List<Meeting>,
        tasks: List<Task>,
        exportedAt: Long = System.currentTimeMillis()
    ): String {
        val confirmedMeetings = meetings.filter { it.isConfirmed }
        val confirmedMeetingIds = confirmedMeetings.map { it.id }.toSet()
        val exportedTasks = tasks.filter { task ->
            task.meetingId == null || task.meetingId in confirmedMeetingIds
        }

        return buildString {
            appendLine("{")
            appendLine("  \"formatVersion\": $BACKUP_FORMAT_VERSION,")
            appendLine("  \"roomSchemaVersion\": $ROOM_SCHEMA_VERSION,")
            appendLine("  \"exportedAt\": $exportedAt,")
            appendLine("  \"exportedAtText\": ${jsonString(readableDate(exportedAt))},")
            appendLine("  \"meetings\": [")
            confirmedMeetings.forEachIndexed { index, meeting ->
                append(meeting.toJson(indent = "    "))
                appendLine(if (index == confirmedMeetings.lastIndex) "" else ",")
            }
            appendLine("  ],")
            appendLine("  \"tasks\": [")
            exportedTasks.forEachIndexed { index, task ->
                append(task.toJson(indent = "    "))
                appendLine(if (index == exportedTasks.lastIndex) "" else ",")
            }
            appendLine("  ]")
            appendLine("}")
        }
    }

    private fun tasksCsv(tasks: List<TaskModel>): String {
        val header = listOf(
            "title",
            "description",
            "assigneeName",
            "dueAt",
            "reminderTime",
            "category",
            "priority",
            "isCompleted",
            "meetingId"
        ).joinToString(",")

        val rows = tasks.map { task ->
            listOf(
                task.title,
                task.description,
                task.assigneeName,
                task.dueAt?.let(::readableDate).orEmpty(),
                task.reminderTime?.let(::readableDate).orEmpty(),
                task.category,
                task.priority,
                task.isCompleted.toString(),
                task.meetingId.orEmpty()
            ).joinToString(",") { it.csvCell() }
        }

        return (listOf(header) + rows).joinToString("\n")
    }

    private fun StringBuilder.appendSection(title: String, content: String) {
        appendLine(title)
        appendLine(content.ifBlank { "--" }.trim())
        appendLine()
    }

    private fun StringBuilder.appendTasks(tasks: List<TaskModel>) {
        appendLine("Linked tasks")
        if (tasks.isEmpty()) {
            appendLine("--")
            appendLine()
            return
        }
        tasks.forEachIndexed { index, task ->
            appendLine("${index + 1}. ${task.title.ifBlank { "Untitled task" }}")
            appendLine("   Assignee: ${task.assigneeName.ifBlank { "Me" }}")
            appendLine("   Deadline: ${task.dueAt?.let(::readableDate) ?: "--"}")
            appendLine("   Reminder: ${task.reminderTime?.let(::readableDate) ?: "--"}")
            appendLine("   Priority: ${task.priority.ifBlank { "Medium" }}")
            appendLine("   Category: ${task.category.ifBlank { "Other" }}")
            appendLine("   Completed: ${task.isCompleted}")
            if (task.description.isNotBlank()) {
                appendLine("   Description: ${task.description}")
            }
        }
        appendLine()
    }

    private fun Meeting.toJson(indent: String): String {
        return buildString {
            appendLine("$indent{")
            appendLine("$indent  \"id\": ${jsonString(id)},")
            appendLine("$indent  \"title\": ${jsonString(title)},")
            appendLine("$indent  \"date\": $date,")
            appendLine("$indent  \"duration\": $duration,")
            appendLine("$indent  \"transcript\": ${jsonString(transcript)},")
            appendLine("$indent  \"audioFilePath\": ${jsonNullableString(audioFilePath)},")
            appendLine("$indent  \"summary\": ${jsonString(summary)},")
            appendLine("$indent  \"decisionsText\": ${jsonString(decisionsText)},")
            appendLine("$indent  \"blockersText\": ${jsonString(blockersText)},")
            appendLine("$indent  \"followUpsText\": ${jsonString(followUpsText)},")
            appendLine("$indent  \"isConfirmed\": $isConfirmed")
            append("$indent}")
        }
    }

    private fun Task.toJson(indent: String): String {
        return buildString {
            appendLine("$indent{")
            appendLine("$indent  \"id\": ${jsonString(id)},")
            appendLine("$indent  \"title\": ${jsonString(title)},")
            appendLine("$indent  \"description\": ${jsonString(description)},")
            appendLine("$indent  \"category\": ${jsonString(category)},")
            appendLine("$indent  \"priority\": ${jsonString(priority)},")
            appendLine("$indent  \"isCompleted\": $isCompleted,")
            appendLine("$indent  \"reminderTime\": ${reminderTime ?: "null"},")
            appendLine("$indent  \"meetingId\": ${jsonNullableString(meetingId)},")
            appendLine("$indent  \"assigneeName\": ${jsonString(assigneeName)},")
            appendLine("$indent  \"dueAt\": ${dueAt ?: "null"}")
            append("$indent}")
        }
    }

    private fun String.csvCell(): String {
        return "\"${replace("\"", "\"\"").replace("\r\n", "\n").replace("\r", "\n")}\""
    }

    private fun jsonString(value: String): String {
        return "\"${value.jsonEscaped()}\""
    }

    private fun jsonNullableString(value: String?): String {
        return value?.let(::jsonString) ?: "null"
    }

    private fun String.jsonEscaped(): String {
        return buildString {
            this@jsonEscaped.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }
    }

    private fun readableDate(millis: Long): String {
        return readableDateFormatter.format(Date(millis))
    }
}
