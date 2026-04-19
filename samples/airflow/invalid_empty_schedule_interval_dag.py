from datetime import datetime

from airflow import DAG
from airflow.operators.empty import EmptyOperator


with DAG(
    dag_id="invalid_empty_schedule_interval_demo",
    start_date=datetime(2024, 1, 1),
    schedule_interval="   ",
    catchup=False,
) as dag:
    EmptyOperator(task_id="start")
