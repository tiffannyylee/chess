package dataaccess;

public class UserAlreadyExistsException extends DataAccessException {
  public UserAlreadyExistsException(String message) {
    super(message);
  }
}
