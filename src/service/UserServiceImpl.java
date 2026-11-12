package service;

import model.User;
import exception.InvalidUserException;

public class UserServiceImpl implements UserService {

    @Override
    public boolean validateUser(User user) throws InvalidUserException {

    if (user == null) {
        throw new InvalidUserException("User cannot be null.");
    }

    if (!user.validateUser()) {
        throw new InvalidUserException("Invalid user details.");
    }

    return true;
}

    @Override
    public boolean authenticateUser(User user, String username, String password) {

        if (user == null) {
            return false;
        }

        return user.getUsername().equals(username)
                && user.getPassword().equals(password);
    }
}