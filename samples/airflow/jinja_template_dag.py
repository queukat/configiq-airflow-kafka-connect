from datetime import datetime

from airflow import DAG
from airflow.providers.common.sql.operators.sql import SQLExecuteQueryOperator


with DAG(
    dag_id="jinja_template_demo",
    start_date=datetime(2024, 1, 1),
    schedule="@daily",
    catchup=False,
) as dag:
    SQLExecuteQueryOperator(
        task_id="templated_query",
        conn_id="warehouse",
        sql="select * from events where ds = '{{ ds }}'",
    )

