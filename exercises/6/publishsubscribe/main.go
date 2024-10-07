package main

import (
	"context"
	"fmt"
	"log/slog"
	"os"
	"strings"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

var log = slog.Default()

func main() {
	if len(os.Args) < 2 {
		fmt.Printf("usage: %s [command]\n", os.Args[0])
		os.Exit(2)
	}

	ctx := context.Background()
	command := os.Args[1]
	switch command {
	case "emitlog":
		err := publishSubscribeExampleEmitLog(ctx, bodyFrom(os.Args[2:]))
		if err != nil {
			fmt.Printf("error: %s\n", err)
			os.Exit(1)
		}
	case "receivelogs":
		err := publishSubscribeExampleReceiveLogs(ctx)
		if err != nil {
			fmt.Printf("error: %s\n", err)
			os.Exit(1)
		}
	default:
		fmt.Printf("unknown command: %s\n", command)
		os.Exit(2)
	}
}

func bodyFrom(args []string) string {
	var s string
	if (len(args) < 1) || args[0] == "" {
		s = "hello"
	} else {
		s = strings.Join(args, " ")
	}
	return s
}

func publishSubscribeExampleEmitLog(ctx context.Context, logMessage string) error {
	conn, err := amqp.Dial("amqp://guest:guest@localhost:5672/")
	if err != nil {
		return fmt.Errorf("failed to connect to RabbitMQ: %s", err)
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		return fmt.Errorf("failed to open a channel: %s", err)
	}
	defer ch.Close()

	err = ch.ExchangeDeclare(
		"logs",   // name
		"fanout", // type
		true,     // durable
		false,    // auto-deleted
		false,    // internal
		false,    // no-wait
		nil,      // arguments
	)
	if err != nil {
		return fmt.Errorf("failed to declare an exchange: %s", err)
	}

	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

	err = ch.PublishWithContext(ctx,
		"logs", // exchange
		"",     // routing key
		false,  // mandatory
		false,  // immediate
		amqp.Publishing{
			ContentType: "text/plain",
			Body:        []byte(logMessage),
		})
	if err != nil {
		return fmt.Errorf("failed to publish a message: %w", err)
	}

	log.Info("Sent message", "body", logMessage)

	return nil
}

func publishSubscribeExampleReceiveLogs(ctx context.Context) error {
	conn, err := amqp.Dial("amqp://guest:guest@localhost:5672/")
	if err != nil {
		return fmt.Errorf("failed to connect to RabbitMQ: %s", err)
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		return fmt.Errorf("failed to open a channel: %s", err)
	}
	defer ch.Close()

	err = ch.ExchangeDeclare(
		"logs",   // name
		"fanout", // type
		true,     // durable
		false,    // auto-deleted
		false,    // internal
		false,    // no-wait
		nil,      // arguments
	)
	if err != nil {
		return fmt.Errorf("failed to declare exchange: %s", err)
	}

	q, err := ch.QueueDeclare(
		"",    // name
		false, // durable
		false, // delete when unused
		true,  // exclusive
		false, // no-wait
		nil,   // arguments
	)
	if err != nil {
		return fmt.Errorf("failed to declare queue: %s", err)
	}

	err = ch.QueueBind(
		q.Name, // queue name
		"",     // routing key
		"logs", // exchange
		false,
		nil,
	)
	if err != nil {
		return fmt.Errorf("failed to bind a queue: %s", err)
	}

	msgs, err := ch.Consume(
		q.Name, // queue
		"",     // consumer
		true,   // auto-ack
		false,  // exclusive
		false,  // no-local
		false,  // no-wait
		nil,    // args
	)
	if err != nil {
		return fmt.Errorf("failed to register a consumer: %s", err)
	}

	var forever chan struct{}
	go func() {
		for d := range msgs {
			log.Info("Received message", "body", d.Body)
		}
	}()
	log.Info("Waiting for logs. To exit press CTRL+C")
	<-forever

	return nil
}
