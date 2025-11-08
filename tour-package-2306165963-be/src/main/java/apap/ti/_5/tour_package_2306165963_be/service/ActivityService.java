package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Activity;

import java.util.List;
import java.util.Optional;

public interface ActivityService {
    List<Activity> getAllActivities();
    Optional<Activity> getActivityById(String id);
    Activity createActivity(Activity activity);
    Activity updateActivity(Activity activity);
    boolean deleteActivity(String id);
    List<Activity> getActivitiesByActivityType(String type);
    List<Activity> searchActivitiesByName(String name);
}