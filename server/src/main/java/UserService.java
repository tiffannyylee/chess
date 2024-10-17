import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {this.dataAccess = dataAccess;}

    public AuthData register(UserData newUser) throws ServiceException {
        if (dataAccess.getUser(newUser.username())!=null){
            throw new ServiceException("User already exists");
        }
        dataAccess.createUser(newUser);
        return newUser;
    }
    public AuthData login(UserData user) {}
    public void logout(AuthData auth) {}
}
