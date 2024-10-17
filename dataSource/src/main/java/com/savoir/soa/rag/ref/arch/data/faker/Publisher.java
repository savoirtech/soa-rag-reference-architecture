/*
 * Copyright (c) 2012-2024 Savoir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.savoir.soa.rag.ref.arch.data.faker;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import net.datafaker.Faker;
import net.datafaker.providers.base.Address;
import net.datafaker.providers.base.Name;
import net.datafaker.transformations.JsonTransformer;
import net.datafaker.transformations.Schema;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import static net.datafaker.transformations.Field.field;

public class Publisher {

    public static void main(String []args) throws JMSException {

        String user = env("ACTIVEMQ_USER", "admin");
        String password = env("ACTIVEMQ_PASSWORD", "password");
        String host = env("ACTIVEMQ_HOST", "localhost");
        int port = Integer.parseInt(env("ACTIVEMQ_PORT", "61616"));
        String destination = arg(args, 0, "reservations");

        int messages = 10000;

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);

        Connection connection = factory.createConnection(user, password);

        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = new ActiveMQQueue(destination);
        MessageProducer producer = session.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        Faker faker = new Faker();

        for( int i=1; i <= messages; i ++) {

            Name name = faker.name();
            Address address = faker.address();
            Schema<Object, ?> schema = Schema.of(
                    field("firstName", () -> name.firstName()),
                    field("lastName", () ->  name.lastName()),
                    field("streetName", () ->  address.streetName()),
                    field("streetAddress", () ->  address.streetAddress()),
                    field("zipcode", () ->  address.zipCode()),
                    field("email", () ->  faker.internet().emailAddress()),
                    field("cell", () ->  faker.phoneNumber().cellPhone())
            );

            JsonTransformer<Object> transformer = JsonTransformer.builder().build();
            String json = transformer.generate(schema, 1);

            TextMessage msg = session.createTextMessage(json);
            msg.setIntProperty("id", i);
            producer.send(msg);
            if( (i % 1000) == 0) {
                System.out.println(String.format("Sent %d messages", i));
            }
        }

        producer.send(session.createTextMessage("SHUTDOWN"));
        connection.close();

    }

    private static String env(String key, String defaultValue) {
        String rc = System.getenv(key);
        if( rc== null )
            return defaultValue;
        return rc;
    }

    private static String arg(String []args, int index, String defaultValue) {
        if( index < args.length )
            return args[index];
        else
            return defaultValue;
    }

}
