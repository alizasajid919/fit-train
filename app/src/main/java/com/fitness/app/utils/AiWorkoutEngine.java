package com.fitness.app.utils;

import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.WorkoutExerciseItem;
import com.fitness.app.models.WorkoutSession;

import java.util.List;
import java.util.Locale;

public class AiWorkoutEngine {

    /**
     * Evaluates actual workout session metrics and populates data-driven AI Performance Analysis
     * and AI Recommendations on the session object.
     */
    public static void generateAnalysisAndRecommendations(WorkoutSession session, LocalDataManager localDb) {
        if (session == null) return;

        double completionPct = session.getCompletionPercentage();
        int completedEx = session.getCompletedExercisesCount();
        int partialEx = session.getPartiallyCompletedExercisesCount();
        int skippedEx = session.getSkippedExercisesCount();
        int totalEx = session.getTotalPlannedExercises();

        int actualReps = session.getActualCompletedReps();
        int plannedReps = session.getTotalPlannedReps();
        int actualSets = session.getCompletedSetsCount();
        int plannedSets = session.getTotalPlannedSets();

        long activeSecs = session.getActiveDurationMs() / 1000;
        long mins = activeSecs / 60;
        long secs = activeSecs % 60;

        int estCals = session.getEstimatedCalories();
        String rpe = session.getUserRpe();

        // 1. Generate Truthful AI Performance Analysis
        StringBuilder analysis = new StringBuilder();

        if (session.isStoppedEarly()) {
            analysis.append(String.format(Locale.getDefault(),
                "Workout stopped early. You completed %.1f%% of your planned session (%d of %d exercises).\n\n",
                completionPct, completedEx + partialEx, totalEx));
        } else {
            analysis.append(String.format(Locale.getDefault(),
                "Session Finished! You achieved %.1f%% completion across %d planned exercises.\n\n",
                completionPct, totalEx));
        }

        // Workload Breakdown
        if (plannedReps > 0) {
            analysis.append(String.format(Locale.getDefault(),
                "• Repetitions: %d valid completed out of %d planned reps.\n", actualReps, plannedReps));
        }
        if (session.getInvalidRepsCount() > 0) {
            analysis.append(String.format(Locale.getDefault(),
                "• Form Corrections: %d incomplete movement attempts logged.\n", session.getInvalidRepsCount()));
        }
        if (plannedSets > 0) {
            analysis.append(String.format(Locale.getDefault(),
                "• Sets Volume: %d completed out of %d planned sets.\n", actualSets, plannedSets));
        }

        analysis.append(String.format(Locale.getDefault(),
            "• Active Duration: %dm %ds active exercise time.\n", mins, secs));

        analysis.append(String.format(Locale.getDefault(),
            "• Energy Expenditure: ~%d estimated kcal burned based on your body weight.\n", estCals));

        // Sensor Truth
        analysis.append("• Heart Rate: ").append(session.getHeartRateStatus()).append("\n");
        analysis.append("• Steps & Distance: ").append(session.getStepsStatus());

        session.setAiPerformanceAnalysis(analysis.toString());

        // 2. Generate Data-Driven Personalized Recommendations
        StringBuilder rec = new StringBuilder();

        // Skipped exercises warning
        if (skippedEx > 0 && session.getExercises() != null) {
            rec.append("• Skipped Exercises: ");
            int count = 0;
            for (WorkoutExerciseItem item : session.getExercises()) {
                if (item.getStatus() == WorkoutExerciseItem.Status.SKIPPED) {
                    if (count > 0) rec.append(", ");
                    rec.append(item.getName());
                    count++;
                }
            }
            rec.append(". Consider targeting these in your next session when fresh.\n\n");
        }

        // Performance progression & recovery advice
        if (completionPct >= 95.0) {
            rec.append("• Training Progression: Outstanding consistency! You completed all planned volume. Progressive overload (adding 1-2 reps or 5% resistance) is recommended for your next workout.\n\n");
        } else if (completionPct >= 60.0) {
            rec.append("• Training Adjustment: Great effort! You completed over half the planned workload. Rest 24 hours before attempting remaining muscle groups to avoid fatigue.\n\n");
        } else {
            rec.append("• Recovery Advice: You stopped before completing the full routine. Lower intensity or allow longer rest periods between sets to build endurance gradually.\n\n");
        }

        // RPE Feedback Adjustment
        if ("Very Difficult".equalsIgnoreCase(rpe)) {
            rec.append("• Perceived Exertion: You rated this session as 'Very Difficult'. Hold current weight/reps steady for your next 2 workouts before increasing difficulty.\n\n");
        } else if ("Easy".equalsIgnoreCase(rpe) && completionPct >= 90.0) {
            rec.append("• Challenge Rating: You rated this workout 'Easy'. Increase set count or exercise duration to maintain progressive adaptation.\n\n");
        }

        // Form posture notes check
        boolean hasFormIssue = false;
        if (session.getExercises() != null) {
            for (WorkoutExerciseItem item : session.getExercises()) {
                if (item.getFormNotes() != null && !item.getFormNotes().isEmpty()) {
                    if (!hasFormIssue) {
                        rec.append("• Form Correction Notes:\n");
                        hasFormIssue = true;
                    }
                    rec.append("  - ").append(item.getName()).append(": ").append(item.getFormNotes()).append("\n");
                }
            }
        }

        // Previous Session Comparison
        if (localDb != null) {
            WorkoutSession prevSession = localDb.getPreviousWorkoutSession(session.getWorkoutTitle(), session.getSessionId());
            if (prevSession != null) {
                rec.append(String.format(Locale.getDefault(),
                    "\n• Previous Session Comparison: Completion changed from %.1f%% (%s) to %.1f%% today.",
                    prevSession.getCompletionPercentage(), prevSession.getDateStr(), completionPct));
            } else {
                rec.append("\n• Previous Session Comparison: This is your first recorded session for this workout type.");
            }
        }

        session.setAiRecommendations(rec.toString());
    }
}
