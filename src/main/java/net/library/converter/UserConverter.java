package net.library.converter;

import net.library.model.entity.User;
import net.library.model.request.UserRequest;

public class UserConverter {

    public static User of(final UserRequest userRequest){

       final  var user = new User();
        user.setUsername(userRequest.getUsername());
        user.setName(userRequest.getName());
        user.setSurname(userRequest.getSurname());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setAddress(userRequest.getAddress());

        return user;
    }
}
