package com.wipro.demo.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.wipro.demo.bean.User;

@Service
public class UserServiceImp implements UserService {

	private static List<User> users;

	static {
		users = dummyUsers();
	}

	public List<User> getUser() {
		// TODO Auto-generated method stub
		return users;
	}

	public User findById(int id) {
		// TODO Auto-generated method stub
		for (User user : users) {
			if (user.getId() == id) {
				return user;
			}
		}
		return null;
	}

	public void createUser(User user) {
		// TODO Auto-generated method stub
		users.add(user);
	}

	public void deleteUserById(int id) {
		// TODO Auto-generated method stub
		Iterator<User> it = users.iterator();
		while (it.hasNext()) {
			User user = (User) it.next();
			if (user.getId() == id) {
				it.remove();
			}
		}
	}

	public void updatePartially(User currentUser, int id) {
		for (User user : users) {
			if (user.getId() == id) {
				if (currentUser.getCountry() != null) {
					user.setId(id);
					user.setCountry(currentUser.getCountry());
				}
				user.setName(user.getName());
				update(user);
			}
		}

	}

	public void update(User user) {
		// TODO Auto-generated method stub
		int index = users.indexOf(user);
		users.set(index, user);
	}

	private static List<User> dummyUsers() {
		// TODO Auto-generated method stub
		List<User> users = new ArrayList<User>();
		users.add(new User(1, "Hema", "INDIA"));
		users.add(new User(2, "Shubham", "UK"));
		users.add(new User(3, "Pooja", "INDIA"));
		users.add(new User(4, "Vaishu", "USA"));
		return users;
	}

}