package com.crio.qcontest.services;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.crio.qcontest.entities.Contest;
import com.crio.qcontest.entities.Contestant;
import com.crio.qcontest.entities.DifficultyLevel;
import com.crio.qcontest.entities.Question;
import com.crio.qcontest.entities.User;
import com.crio.qcontest.repositories.IContestRepository;
import com.crio.qcontest.repositories.IContestantRepository;
import com.crio.qcontest.repositories.IQuestionRepository;
import com.crio.qcontest.repositories.IUserRepository;

public class ContestService{
    private final IContestantRepository contestantRepository;
    private final IContestRepository contestRepository;
    private final IQuestionRepository questionRepository;
    private final IUserRepository userRepository;

    public ContestService(IContestantRepository contestantRepository, IContestRepository contestRepository,
            IQuestionRepository questionRepository, IUserRepository userRepository) {
        this.contestantRepository = contestantRepository;
        this.contestRepository = contestRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new contest with specified parameters.
     * @param name Name of the contest.
     * @param level Difficulty level of the contest.
     * @param userId ID of the user creating the contest.
     * @param numOfQuestions Number of questions in the contest.
     * @return Created Contest object.
     * @throws RuntimeException if user is not found or requested questions cannot be fulfilled.
     */
    public Contest createContest(String name, DifficultyLevel level, Long userId, Integer numOfQuestions) {
        Optional<User> user = userRepository.findById(userId);
            if(!user.isPresent()){
                throw new RuntimeException("User with an id: "+ userId+ " not found!");
            }

        List<Question> availableQuestions = questionRepository.findByDifficultyLevel(level);
        if (availableQuestions.size() < numOfQuestions) {
            throw new RuntimeException("Requested number of questions: " + numOfQuestions + " cannot be fulfilled!");
        }
        List<Question> selectedQuestions = pickRandomQuestions(availableQuestions, numOfQuestions);
    
        Contest contest = new Contest(name, level, user.get(), selectedQuestions);
        return contestRepository.save(contest);

    }

    // https://www.codespeedy.com/how-to-randomly-select-items-from-a-list-in-java/
    private List<Question> pickRandomQuestions(List<Question> questionList, Integer numQuestions){
        Random rand = new Random(); // object of Random class.
       //temporary list to hold selected items.
        List<Question> tempList = new ArrayList<>(); 
        for (int i = 0; i < numQuestions; i++) { 
            int randomIndex = rand.nextInt(questionList.size());
            // the loop check on repetition of elements
            while(tempList.contains(questionList.get(randomIndex))){
                randomIndex = rand.nextInt(questionList.size());
            }
            tempList.add(questionList.get(randomIndex)); 
        } 
        return tempList; 
    }

    /**
     * Retrieves a list of contests filtered by difficulty level.
     * @param level Difficulty level filter (can be null).
     * @return List of contests filtered by difficulty level.
     */
    public List<Contest> listContests(DifficultyLevel level) {
        return (level == null) ? contestRepository.findAll() : contestRepository.findByDifficultyLevel(level);
    }

    /**
     * Registers a user as a contestant for a contest.
     * @param contestId ID of the contest.
     * @param userId ID of the user registering for the contest.
     * @return Registered Contestant object.
     * @throws RuntimeException if contest or user is not found.
     */
    public Contestant attendContest(Long contestId, Long userId) {

        Optional<Contest> contest = contestRepository.findById(contestId);
        if(!contest.isPresent()){
            throw new RuntimeException("Contest with an id "+ contestId+ " not found!");
        }

        Optional<User> user = userRepository.findById(userId);
        if(!user.isPresent()){
            throw new RuntimeException("User with an id "+ userId+ " not found!");
        }

        Contestant contestant = new Contestant(user.get(), contest.get());
        contestantRepository.save(contestant);
        return contestant;
        // Contest contest = contestRepository.findById(contestId)
        // .orElseThrow(() -> new RuntimeException("Contest with ID " + contestId + " not found!"));
        // User user = userRepository.findById(userId)
        //     .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found!"));
        // Contestant contestant = new Contestant(user, contest);
        // contestantRepository.save(contestant);
        // return contestant;
        
    }

    /**
     * Withdraws a contestant from a contest.
     * @param contestId ID of the contest.
     * @param userId ID of the user withdrawing from the contest.
     * @return True if contestant is successfully withdrawn, false otherwise.
     * @throws RuntimeException if contestant is not found.
     */
    public Boolean withdrawContest(Long contestId, Long userId) {

        Optional<Contestant> contestant = contestantRepository.findById(contestId,userId);
        if(!contestant.isPresent()){
            throw new RuntimeException("Contestant not found!");
        }
        contestantRepository.deleteById(contestId, userId);
        return true;
    }

    /**
     * Executes a contest by selecting random questions for contestants and updating scores.
     * @param contestId ID of the contest.
     * @param userId ID of the user running the contest (contest creator).
     * @throws RuntimeException if contest is not found or user is not the creator.
     */
    public void runContest(Long contestId, Long userId) {
        // Check if contest is valid as per the required conditions.
        Contest contest = contestRepository.findById(contestId).orElseThrow(() -> new RuntimeException("Contest with an id "+contestId+" not found!"));
        if(!contest.getCreator().getId().equals(userId)){
            throw new RuntimeException("Only the contest creator can run the contest!");
        }
        // Get all the contestants who registered for the given contest.
        List<Contestant> contestantList = contestantRepository.findByContestId(contestId);
        // For each contestant,
        contestantList.forEach((contestant)-> {
            // Select random questions from the contest which will be considered as solved by the contestant.
            List<Question> solvedQuestionsList = pickRandomQuestions(contest.getQuestions(),getRandomNumberInRange(0,contest.getQuestions().size()));
            // Store the solved questions in the contestant.
            solvedQuestionsList.forEach((question)->{
                contestant.addQuestion(question);
            });
            User user = contestant.getUser();
            // Generate new totalScore for the user as per the recently solved questions.
            int newScore = contestant.getTotalScore() + user.getScore() - contest.getLevel().getWeight();
            // Update the totalscore of the user.
            user.setScore(newScore);
        });
    }

    // https://mkyong.com/java/java-generate-random-integers-in-a-range/
    private int getRandomNumberInRange(int min, int max) {
		Random r = new Random();
		return r.ints(min, (max + 1)).findFirst().getAsInt();
	}

    /**
     * Retrieves contest history sorted by contestant scores.
     * @param contestId ID of the contest.
     * @return List of contestants sorted by total score in descending order.
     * @throws RuntimeException if contest is not found.
     */
    public List<Contestant> contestHistory(Long contestId) {
        Optional<Contest> contest = contestRepository.findById(contestId);
        if(!contest.isPresent()){
            throw new RuntimeException("Contest with an id " + contestId +" not found!");
        }
        List<Contestant> contestants = contestantRepository.findByContestId(contestId);
        return contestants.stream()
            .sorted((c1, c2) -> Integer.compare(c2.getTotalScore(), c1.getTotalScore())) // Sort in descending order
            .collect(Collectors.toList());
    }  
}