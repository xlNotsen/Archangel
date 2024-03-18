package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.DatabaseLoggable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@AllArgsConstructor
public class CatalogPurchaseLogEntry implements Runnable, DatabaseLoggable {

    private static final String QUERY = "INSERT INTO `logs_shop_purchases` (timestamp, user_id, catalog_item_id, item_ids, catalog_name, cost_credits, cost_points, points_type, amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final int timestamp;
    private final int userId;
    private final int catalogItemId;
    private final String itemIds;
    private final String catalogName;
    private final int costCredits;
    private final int costPoints;
    private final int pointsType;
    private final int amount;

    @Override
    public String getQuery() {
        return QUERY;
    }

    @Override
    public void log(PreparedStatement statement) throws SQLException {
        statement.setInt(1, this.timestamp);
        statement.setInt(2, this.userId);
        statement.setInt(3, this.catalogItemId);
        statement.setString(4, this.itemIds);
        statement.setString(5, this.catalogName);
        statement.setInt(6, this.costCredits);
        statement.setInt(7, this.costPoints);
        statement.setInt(8, this.pointsType);
        statement.setInt(9, this.amount);
        statement.addBatch();
    }

    @Override
    public void run() {
        Emulator.getDatabaseLogger().store(this);
    }
}
