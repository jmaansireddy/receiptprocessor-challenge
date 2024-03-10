package com.example.receiptprocessor.controller;

import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    // In-memory storage for receipts
    private final Map<String, Receipt> receiptMap = new HashMap<>();

    @PostMapping("/process")
    public ResponseEntity<?> processReceipt(@RequestBody Receipt receipt) {
        String id = processReceiptLogic(receipt);
        return ResponseEntity.ok(Collections.singletonMap("id", id));
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<?> getPoints(@PathVariable String id) {
        int points = calculatePoints(id);
        return ResponseEntity.ok(Collections.singletonMap("points", points));
    }

    private String processReceiptLogic(Receipt receipt) {
        // Generate a unique ID for the receipt
        String id = UUID.randomUUID().toString();
        // Store the receipt in memory with the generated ID
        receiptMap.put(id, receipt);
        return id;
    }

    private int calculatePoints(String id) {
        // Retrieve the receipt from the in-memory storage based on the ID
        Receipt receipt = receiptMap.get(id);
        if (receipt == null) {
            // If receipt with the provided ID is not found, return 0 points
            return 0;
        }

        // Calculate points based on the receipt details
        int points = calculatePointsForReceipt(receipt);
        return points;
    }

    private int calculatePointsForReceipt(Receipt receipt) {
        int points = 0;

        // Rule 1: One point for every alphanumeric character in the retailer name
        points += receipt.getRetailer().replaceAll("[^a-zA-Z0-9]", "").length();

        // Rule 2: 50 points if the total is a round dollar amount with no cents
        double total = Double.parseDouble(receipt.getTotal());
        if (total == Math.floor(total)) {
            points += 50;
        }

        // Rule 3: 25 points if the total is a multiple of 0.25
        if (total % 0.25 == 0) {
            points += 25;
        }

        // Rule 4: 5 points for every two items on the receipt
        int itemCount = receipt.getItems().size();
        points += (itemCount / 2) * 5;

        // Rule 5: If the trimmed length of the item description is a multiple of 3, multiply the price by 0.2 and round up to the nearest integer.
        for (Item item : receipt.getItems()) {
            int descriptionLength = item.getShortDescription().trim().length();
            if (descriptionLength % 3 == 0) {
                double itemPrice = Double.parseDouble(item.getPrice());
                int itemPoints = (int) Math.ceil(itemPrice * 0.2);
                points += itemPoints;
            }
        }

        // Rule 6: 6 points if the day in the purchase date is odd
        String[] dateParts = receipt.getPurchaseDate().split("-");
        int day = Integer.parseInt(dateParts[2]);
        if (day % 2 != 0) {
            points += 6;
        }

        // Rule 7: 10 points if the time of purchase is after 2:00pm and before 4:00pm
        String[] timeParts = receipt.getPurchaseTime().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        if (hour >= 14 && hour < 16) {
            points += 10;
        }

        return points;
    }
}
