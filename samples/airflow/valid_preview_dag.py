from datetime import datetime

from airflow import DAG
from airflow.operators.empty import EmptyOperator


with DAG(
    dag_id="valid_preview_demo",
    start_date=datetime(2024, 1, 1),
    schedule="0 6 * * 1-5",
    catchup=False,
) as dag:
    EmptyOperator(task_id="preview_target")
