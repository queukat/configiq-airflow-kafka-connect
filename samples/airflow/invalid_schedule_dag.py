from datetime import datetime

from airflow import DAG
from airflow.operators.empty import EmptyOperator


with DAG(
    dag_id="invalid_schedule_demo",
    start_date=datetime(2024, 1, 1),
    schedule="0 12 * *",
    catchup=False,
) as dag:
    EmptyOperator(task_id="start")

