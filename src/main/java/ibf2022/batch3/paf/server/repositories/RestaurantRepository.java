package ibf2022.batch3.paf.server.repositories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import ibf2022.batch3.paf.server.models.Comment;
import ibf2022.batch3.paf.server.models.Restaurant;

@Repository
public class RestaurantRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	// TODO: Task 2
	// Do not change the method's signature
	// Write the MongoDB query for this method in the comments below
	// db.restaurants.distinct("cuisine")
	public List<String> getCuisines() {
		List<String> strings = mongoTemplate.findDistinct(new Query(), "cuisine", "AllRestaurants", String.class);
		System.out.println("Cuisines are " + strings);
		return strings;
	}

	// TODO: Task 3
	// Do not change the method's signature
	// Write the MongoDB query for this method in the comments below
	// db.restaurants.find({cuisine:"American "},{_id:0,"name":1,"restaurant_id":1})
	public List<Restaurant> getRestaurantsByCuisine(String cuisine) {
        Query query = new Query(Criteria.where("cuisine").is(cuisine));
        query.fields().exclude("_id").include("name", "restaurant_id");
        List<Document> results = mongoTemplate.find(query, Document.class, "AllRestaurants");

		// Get all restaurants in the collection
		List<Document> restaurants = mongoTemplate.findAll(Document.class, "AllRestaurants");
		List<Restaurant> restaurantList = new ArrayList<>();

        for (Document doc : results) {
            Restaurant newRest = new Restaurant();
            newRest.setName(doc.getString("name"));
            newRest.setRestaurantId(doc.getString("restaurant_id"));
            restaurantList.add(newRest);
        }
        restaurantList.sort(Comparator.comparing(Restaurant::getName));
        return restaurantList;
    }

	// TODO: Task 4
	// Do not change the method's signature
	// Write the MongoDB query for this method in the comments below
	// db.restaurants.findOne({_id: ObjectId(id)})
	public Optional<Restaurant> getRestaurantById(String id) {
		MatchOperation mOp = Aggregation.match(Criteria.where("restaurant_id").is(id));
		LookupOperation lOp = Aggregation.lookup("comment", "restaurant_id", "restaurantId",
				"comment");

		List<Document> resultList = mongoTemplate
				.aggregate(Aggregation.newAggregation(mOp, lOp), "AllRestaurants", Document.class)
				.getMappedResults();

		System.out.println("Result list is " + resultList);

		if (resultList == null || resultList.size() == 0) {
			return Optional.empty();
		}

		// convert to restaurant
		// should only have 1 result
		Document d = resultList.get(0);

		Restaurant r = convertDocumentToRestaurant(d);
		// comments
		List<Document> commentDocuments = d.getList("comment", Document.class);
		List<Comment> comments = new ArrayList();

		commentDocuments.forEach(comment -> {
			Comment c = new Comment();
			c.setRestaurantId(comment.getString("restaurantId"));
			c.setName(comment.getString("name"));
			c.setDate(comment.getLong("date"));
			c.setComment(comment.getString("comment"));
			c.setRating(comment.getInteger("rating"));
			comments.add(c);
		});
		r.setComments(comments);

		return Optional.of(r);
	}

	// helper methods
	private Restaurant convertDocumentToRestaurant(Document d) {
		Restaurant r = new Restaurant();
		r.setRestaurantId(d.getString("restaurant_id"));
		r.setName(d.getString("name"));
		r.setCuisine(d.getString("cuisine"));

		List<String> addr = new ArrayList<>();
		addr.add(d.getEmbedded(List.of("address", "building"), String.class));
		addr.add(d.getEmbedded(List.of("address", "street"), String.class));
		addr.add(d.getEmbedded(List.of("address", "zipcode"), String.class));
		addr.add(d.getString("borough"));
		r.setAddress(String.join(", ", addr));
		return r;
	}

	// TODO: Task 5
	// Do not change the method's signature
	// Write the MongoDB query for this method in the comments below
	// db.restaurants_comment.insert(
	//       {
	//       "restaurant_id": "40356068",
	//       "name" : "Zohan",
	//       "date": new Date(),
	//       "comment": "a fizzly bubbly time",
	//       "rating": 4
	//       })

	public void insertRestaurantComment(Comment comment) {
		mongoTemplate.insert(comment, "comment");
	}
}
