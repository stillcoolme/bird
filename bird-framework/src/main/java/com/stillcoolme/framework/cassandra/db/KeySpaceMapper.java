package com.stillcoolme.framework.cassandra.db;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.DaoTable;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface KeySpaceMapper {
    // 只指定 keyspace 名， 默认去找和 entity 相同名的 table，这里实体叫 Product，就找 product 表
    @DaoFactory
    ProductDao productDao(@DaoKeyspace CqlIdentifier keyspace);

    @DaoFactory
    ProductDao productDao(@DaoKeyspace CqlIdentifier keyspace, @DaoTable CqlIdentifier table);
}
