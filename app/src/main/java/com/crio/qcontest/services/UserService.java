package com.crio.qcontest.services;


import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.crio.qcontest.entities.User;
import com.crio.qcontest.repositories.IUserRepository;

public class UserService{

    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user with the specified name.
     * @param name Name of the user.
     * @return Created User object.
     */
    public User createUser(String name) {
        User user = userRepository.save(new User(name));
        return user;
    }

    /**
     * Retrieves a list of users sorted by their score.
     * @param order Sorting order ("ASC" for ascending, "DESC" for descending).
     * @return List of users sorted by score as per the specified order.
     */
    public List<User> showLeaderBoard(String order) {
        List<User> users = userRepository.findAll();
        
        if (order.equalsIgnoreCase("ASC")) {
            return users.stream()
                    .sorted(Comparator.comparingInt(User::getScore))
                    .collect(Collectors.toList());
        } else { // Default to DESC if order is not "ASC"
            return users.stream()
                    .sorted(Comparator.comparingInt(User::getScore).reversed())
                    .collect(Collectors.toList());
        }
    } 
}