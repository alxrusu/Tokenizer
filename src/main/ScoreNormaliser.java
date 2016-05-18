package main;

import java.util.ArrayList;

public class ScoreNormaliser {

    public static final float MIN_SCORE = -5f;
    public static final float MAX_SCORE = 5f;

    public static void AverageDistribution (ArrayList<Tuple> scoreList) {

        float scor_minim = 9999999;
        float scor_maxim = -1;
        float scor_avg = 0f;

        for (Tuple tuple : scoreList) {

            scor_avg += tuple.score;
            if (tuple.score < scor_minim)
                scor_minim = tuple.score;
            if (tuple.score > scor_maxim)
                scor_maxim = tuple.score;

        }
        scor_avg /= scoreList.size();

        float score;
        for (Tuple tuple : scoreList) {
            score = tuple.score;
            if (score >= scor_avg) {
                score = (score - scor_avg) / (scor_maxim - scor_avg) * (MAX_SCORE - (MAX_SCORE + MIN_SCORE) / 2) + (MAX_SCORE + MIN_SCORE) / 2;
            }
            else {
                score = (score - scor_avg) / (scor_avg - scor_minim) * (MAX_SCORE - (MAX_SCORE + MIN_SCORE) / 2) + (MAX_SCORE + MIN_SCORE) / 2;
            }
            tuple.score = score;
        }

    }

    public static void UniformDistribution (ArrayList<Tuple> scoreList) {

        scoreList.sort ( (o1, o2) -> {
            if (o1.score > o2.score)
                return 1;
            if (o1.score < o2.score)
                return -1;
            return 0;
        });

        float score;
        for (int i=0; i<scoreList.size(); i++) {
            score = (MAX_SCORE - (float)(scoreList.size() - i) / (float)scoreList.size() * (MAX_SCORE - MIN_SCORE));
            scoreList.get(i).score = score;
        }

    }

}
