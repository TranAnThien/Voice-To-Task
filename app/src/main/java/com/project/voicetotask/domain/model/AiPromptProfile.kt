package com.project.voicetotask.domain.model

enum class AiPromptProfile(
    val id: String,
    val displayName: String,
    val description: String,
    val promptGuidance: String
) {
    GENERAL_MEETING(
        id = "general_meeting",
        displayName = "General Meeting",
        description = "Balanced analysis for common meetings.",
        promptGuidance = """
            Treat the transcript as a general meeting.
            Balance summary, key decisions, blockers/risks, follow-ups, and actionable tasks.
            Prefer concrete commitments over vague discussion.
        """.trimIndent()
    ),
    DAILY_STANDUP(
        id = "daily_standup",
        displayName = "Daily Standup",
        description = "Focus on yesterday, today, blockers, and next tasks.",
        promptGuidance = """
            Treat the transcript as a daily standup.
            Prioritize what was done yesterday, what will be done today, blockers, dependencies, and follow-up actions.
            Blockers are especially important.
            If someone says they will do something today, make it a task.
            If the speaker says "today I will" and no name is clear, use "Me" as assignee.
        """.trimIndent()
    ),
    PROJECT_PLANNING(
        id = "project_planning",
        displayName = "Project Planning",
        description = "Focus on milestones, owners, deadlines, decisions, and dependencies.",
        promptGuidance = """
            Treat the transcript as project planning.
            Prioritize milestones, deliverables, deadlines, task owners, decisions, dependencies, risks, and blockers.
            Split tasks by concrete deliverable when possible.
            Do not invent deadlines or owners when they are not stated or strongly implied.
        """.trimIndent()
    ),
    CLASS_LECTURE(
        id = "class_lecture",
        displayName = "Class Lecture",
        description = "Focus on key concepts, assignments, due dates, and technical terms.",
        promptGuidance = """
            Treat the transcript as a class lecture or learning session.
            Prioritize key concepts, definitions, assignments, due dates, and technical terms.
            Decisions may be empty if this is not a meeting.
            Tasks should be homework, study actions, exercises, or follow-up learning items.
        """.trimIndent()
    ),
    PERSONAL_NOTE(
        id = "personal_note",
        displayName = "Personal Note",
        description = "Focus on personal tasks, reminders, and ideas.",
        promptGuidance = """
            Treat the transcript as a personal voice note.
            Prioritize personal tasks, reminders, ideas, errands, and commitments.
            Default assigneeName to "Me".
            Decisions, blockers, and follow-ups can be empty unless clearly present.
        """.trimIndent()
    ),
    INTERVIEW(
        id = "interview",
        displayName = "Interview",
        description = "Focus on interview notes, follow-up questions, and actions.",
        promptGuidance = """
            Treat the transcript as an interview or Q&A.
            Prioritize concise summary, candidate/company/customer notes, important answers, follow-up questions, and follow-up actions.
            Tasks should be concrete follow-up actions.
            Do not invent decisions if the transcript is only a question-and-answer conversation.
        """.trimIndent()
    );

    companion object {
        val default: AiPromptProfile = GENERAL_MEETING

        fun fromId(id: String?): AiPromptProfile {
            return entries.firstOrNull { it.id == id } ?: default
        }
    }
}
