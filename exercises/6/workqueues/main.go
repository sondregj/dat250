package main

import (
	"bytes"
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
	case "newtask":
		err := workQueuesTutorialNewTask(ctx, bodyFrom(os.Args[2:]))
		if err != nil {
			fmt.Printf("error: %s\n", err)
			os.Exit(1)
		}
	case "worker":
		err := workQueuesTutorialWorker(ctx)
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

func workQueuesTutorialNewTask(ctx context.Context, task string) error {
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

	q, err := ch.QueueDeclare(
		"task_queue", // name
		true,         // durable
		false,        // delete when unused
		false,        // exclusive
		false,        // no-wait
		nil,          // arguments
	)
	if err != nil {
		return fmt.Errorf("failed to declare a queue: %s", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	err = ch.PublishWithContext(ctx,
		"",     // exchange
		q.Name, // routing key
		false,  // mandatory
		false,
		amqp.Publishing{
			DeliveryMode: amqp.Persistent,
			ContentType:  "text/plain",
			Body:         []byte(task),
		})
	if err != nil {
		return fmt.Errorf("failed to publish a message: %s", err)
	}

	log.Info("Sent message", "body", task)

	return nil
}

func workQueuesTutorialWorker(ctx context.Context) error {
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

	q, err := ch.QueueDeclare(
		"task_queue", // name
		true,         // durable
		false,        // delete when unused
		false,        // exclusive
		false,        // no-wait
		nil,          // arguments
	)
	if err != nil {
		return fmt.Errorf("failed to declare a queue: %s", err)
	}

	err = ch.Qos(
		1,     // prefetch count
		0,     // prefetch size
		false, // global
	)
	if err != nil {
		return fmt.Errorf("failed to set QoS: %s", err)
	}

	msgs, err := ch.Consume(
		q.Name, // queue
		"",     // consumer
		false,  // auto-ack
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
			log.Info("Received a message", "body", d.Body)
			dotCount := bytes.Count(d.Body, []byte("."))
			t := time.Duration(dotCount)
			time.Sleep(t * time.Second)
			log.Info("Done")

			d.Ack(false)
		}
	}()

	log.Info("Waiting for messages. To exit press CTRL+C")

	<-forever

	return nil
}
