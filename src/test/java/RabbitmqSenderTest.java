import org.datadiode.black.DatadiodeBlackStarter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * Created by marcelmaatkamp on 29/09/15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DatadiodeBlackStarter.class)
@EnableAutoConfiguration(exclude={RabbitAutoConfiguration.class})
public class RabbitmqSenderTest {

    @Autowired
    TopicExchange exchange;

    @Autowired
    RabbitTemplate rabbitTemplateExternal;

    @Test
    public void testSendOneMessage() {
        rabbitTemplateExternal.convertAndSend(exchange.getName(), "spring-boot", "boe", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties properties = message.getMessageProperties();
                properties.setHeader("date", new Date());
                return message;
            }
        });
    }

    @Test
    public void testSendOneBigMessage() {
        int l = 5000;

        byte[] msg = new byte[l];
        for(int i = 0; i < l; i++) {
            msg[i] = (byte) (i%2==0?'A':'B');
        }
        rabbitTemplateExternal.convertAndSend(exchange.getName(), "spring-boot", msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties properties = message.getMessageProperties();
                properties.setHeader("date", new Date());
                return message;
            }
        });
    }

}
