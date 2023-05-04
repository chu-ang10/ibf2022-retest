package ibf2022.batch3.paf.server.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ibf2022.batch3.paf.server.models.Comment;
import ibf2022.batch3.paf.server.models.Restaurant;
import ibf2022.batch3.paf.server.repositories.RestaurantRepository;

@Service
public class RestaurantService {

	@Autowired
    RestaurantRepository restaurantRepo;

	// TODO: Task 2
	// Do not change the method's signature
	public List<String> getCuisines() {
		List<String> cuisineList = restaurantRepo.getCuisines();
        List<String> newList = new ArrayList<>();
        for (String cuisine : cuisineList) {
            cuisine = (cuisine.replace("/", "_"));
			newList.add(cuisine);
        }
        Collections.sort(newList);
        return newList;
	}

	// TODO: Task 3 
	// Do not change the method's signature
	public List<Restaurant> getRestaurantsByCuisine(String cuisine) {
		return restaurantRepo.getRestaurantsByCuisine(cuisine);
	}

	// TODO: Task 4 
	// Do not change the method's signature
	public Optional<Restaurant> getRestaurantById(String id) {
        return restaurantRepo.getRestaurantById(id);
	}

	// TODO: Task 5 
	// Do not change the method's signature
	public void postRestaurantComment(Comment comment) {
		Date d = new Date();
        long epoch = d.getTime();
        comment.setDate(epoch);
        restaurantRepo.insertRestaurantComment(comment);
    }	
}
