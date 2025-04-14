package com.crio.qcontest.services;


import java.util.List;

import com.crio.qcontest.entities.DifficultyLevel;
import com.crio.qcontest.entities.Question;
import com.crio.qcontest.repositories.IQuestionRepository;

public class QuestionService{

    private final IQuestionRepository questionRepository;

    public QuestionService(IQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    /**
     * Creates a new question with specified parameters.
     * @param title Title of the question.
     * @param level Difficulty level of the question.
     * @param difficultyScore Difficulty score of the question.
     * @return Created Question object.
     */
    public Question createQuestion(String title,DifficultyLevel level, Integer difficultyScore) {
        // Create a new Question object with the provided parameters.

        Question newQuestion = new Question(title,level,difficultyScore);


        // Save the newly created Question object to the repository.

        Question question = questionRepository.save(newQuestion);


        // Return the newly created Question

        return question;
        //return null;
    }

    /**
     * Retrieves a list of questions filtered by difficulty level.
     * @param level Difficulty level filter (can be null).
     * @return List of questions filtered by difficulty level.
     */
    public List<Question> listQuestions(DifficultyLevel level) {
        if (level == null) {
            return questionRepository.findAll();
        }
        return questionRepository.findByDifficultyLevel(level);
    }
}