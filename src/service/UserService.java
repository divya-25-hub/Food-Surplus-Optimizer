package service;

import exception.InvalidUserException;
import model.User;

public interface UserService {

    boolean validateUser(User user) throws InvalidUserException;

    boolean authenticateUser(User user, String username, String password);
}