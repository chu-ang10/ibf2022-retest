package ibf2022.batch3.paf.server.controllers;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ibf2022.batch3.paf.server.models.Comment;
import ibf2022.batch3.paf.server.models.Restaurant;
import ibf2022.batch3.paf.server.services.RestaurantService;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

@RestController
public class RestaurantController {
	@Autowired
	RestaurantService restaurantSvc;

	// Task 2 - request handler
	@GetMapping("/api/cuisines")
	public ResponseEntity<String> getCuisines() {
		List<String> cuisineList = restaurantSvc.getCuisines();
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder(cuisineList);
		return ResponseEntity.status(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(jsonArrayBuilder.build().toString());
	}

	// Task 3 - request handler
	@GetMapping("/api/restaurants/{cuisine}")
    public ResponseEntity<String> getRestaurantsbyCuisine(@PathVariable String cuisine) {
        List<Restaurant> resturantList = restaurantSvc.getRestaurantsByCuisine(cuisine);
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (Restaurant rest : resturantList) {
            JsonObjectBuilder jsonOB = Json.createObjectBuilder()
                    .add("restaurantId", rest.getRestaurantId())
                    .add("name", rest.getName());
            System.out.println(rest.getRestaurantId());
            jsonArrayBuilder.add(jsonOB);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonArrayBuilder.build().toString());

    }
	// Task 4 - request handler
	@GetMapping("/api/restaurant/{restaurant_id}")
	public ResponseEntity<String> getRestaurantbyId(@PathVariable String restaurant_id) {
		Optional<Restaurant> restaurantsById = restaurantSvc.getRestaurantById(restaurant_id);

        if (restaurantsById.isEmpty()) {
            String json = Json.createObjectBuilder()
                .add("error", "Missing " + restaurant_id)
                .build().toString();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } else {
            // now convert it to json
            final Restaurant restaurant = restaurantsById.get();
            JsonArrayBuilder comments = Json.createArrayBuilder();
            for (Comment comment : restaurant.getComments()) {
                JsonObjectBuilder jsonOB = Json.createObjectBuilder()
                        .add("restaurantId", comment.getRestaurantId())
                        .add("name", comment.getName())
                        .add("rating", comment.getRating())
                        .add("comment", comment.getComment())
                        .add("date", comment.getDate());
                comments.add(jsonOB);
            }
            JsonObjectBuilder jsonOB = Json.createObjectBuilder()
                    .add("restaurantId", restaurant.getRestaurantId())
                    .add("name", restaurant.getName())
                    .add("cuisine", restaurant.getCuisine())
                    .add("address", restaurant.getAddress())
                    .add("comments", comments);
            
            String json = jsonOB.build().toString();

            return ResponseEntity
        .status(HttpStatus.OK)
    .contentType(MediaType.APPLICATION_JSON)
                .body(json);
        }
	}

	// Task 5 - request handler
	@PostMapping("/api/restaurant/comment")
    public ResponseEntity<Object> postComment(
            @RequestParam("restaurantId") String restaurantId,
            @RequestParam("name") String name,
            @RequestParam("rating") int rating,
            @RequestParam("comment") String comment
    ) {
        if (name == null || name.length() <= 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name must be longer than 3 characters");
        }

        if (rating < 1 || rating > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rating must be between 1 and 5");
        }

        Comment commentObj = new Comment();
        commentObj.setRestaurantId(restaurantId);
        commentObj.setName(name);
        commentObj.setComment(comment);
        commentObj.setRating(rating);
        restaurantSvc.postRestaurantComment(commentObj);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}