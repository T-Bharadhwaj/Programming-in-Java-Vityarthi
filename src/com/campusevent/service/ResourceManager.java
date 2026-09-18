package com.campusevent.service;

import com.campusevent.exception.InvalidInputException;
import com.campusevent.exception.ResourceNotFoundException;
import com.campusevent.model.Resource;
import com.campusevent.model.ResourceStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing physical Resource inventory.
 */
public class ResourceManager {
    private final List<Resource> resources;

    public ResourceManager() {
        this.resources = new ArrayList<>();
    }

    public ResourceManager(List<Resource> initialResources) {
        this.resources = initialResources != null ? new ArrayList<>(initialResources) : new ArrayList<>();
    }

    public void addResource(Resource resource) throws InvalidInputException {
        if (resource == null) {
            throw new InvalidInputException("Resource details cannot be null.");
        }
        if (resource.getId() == null || resource.getId().trim().isEmpty()) {
            throw new InvalidInputException("Resource ID cannot be empty.");
        }
        if (getResourceById(resource.getId()) != null) {
            throw new InvalidInputException("Resource with ID '" + resource.getId() + "' already exists.");
        }
        if (resource.getTotalQuantity() < 0) {
            throw new InvalidInputException("Resource total quantity cannot be negative.");
        }

        resources.add(resource);
    }

    public void updateResource(Resource updatedResource) throws ResourceNotFoundException, InvalidInputException {
        if (updatedResource == null) {
            throw new InvalidInputException("Updated resource cannot be null.");
        }
        Resource existing = getResourceById(updatedResource.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("Resource with ID '" + updatedResource.getId() + "' not found.");
        }
        if (updatedResource.getTotalQuantity() < 0) {
            throw new InvalidInputException("Total quantity cannot be negative.");
        }

        existing.setName(updatedResource.getName());
        existing.setType(updatedResource.getType());
        existing.setTotalQuantity(updatedResource.getTotalQuantity());
        existing.setStatus(updatedResource.getStatus());
        existing.setCostPerUnit(updatedResource.getCostPerUnit());
        existing.setLocation(updatedResource.getLocation());
    }

    public void removeResource(String resourceId) throws ResourceNotFoundException {
        Resource resource = getResourceById(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException("Resource with ID '" + resourceId + "' not found.");
        }
        resources.remove(resource);
    }

    public Resource getResourceById(String resourceId) {
        if (resourceId == null) return null;
        for (Resource r : resources) {
            if (r.getId().equalsIgnoreCase(resourceId.trim())) {
                return r;
            }
        }
        return null;
    }

    public List<Resource> getAllResources() {
        return new ArrayList<>(resources);
    }

    public void setResourceStatus(String resourceId, ResourceStatus status) throws ResourceNotFoundException {
        Resource resource = getResourceById(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException("Resource with ID '" + resourceId + "' not found.");
        }
        resource.setStatus(status);
    }

    public List<Resource> searchResourcesByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllResources();
        }
        String q = query.toLowerCase().trim();
        return resources.stream()
                .filter(r -> r.getName().toLowerCase().contains(q) || r.getId().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }
}
