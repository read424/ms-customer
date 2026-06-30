#!/bin/bash

# Initialize MongoDB database
mongosh -u admin -p bootcamp_accounts_prod_2024 --authenticationDatabase admin <<EOF
use ms_customer_prod;

console.log("Database ms_customer_prod initialized successfully");
EOF
