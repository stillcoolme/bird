package com.stillcoolme.framework.cassandra.db;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.*;

import java.util.UUID;

/**
 * @author: stillcoolme
 * @date: 2019/11/28 11:17
 * @description:
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @PartitionKey
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    private String description;

}
