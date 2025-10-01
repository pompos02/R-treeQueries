import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

# Read the CSV files
range_df = pd.read_csv("rangeQueryResults.csv")
knn_df = pd.read_csv("KNNQueryResults.csv")

# Clean data: Remove rows with missing values if any
range_df = range_df.dropna()
knn_df = knn_df.dropna()

# Convert times to seconds for better readability (optional, but keeping in ms for now)
# range_df['R* Time(s)'] = range_df['R* Time(ms)'] / 1000
# range_df['Sequential Scan Time(s)'] = range_df['Sequential Scan Time(ms)'] / 1000
# knn_df['R* Time(s)'] = knn_df['R* Time(ms)'] / 1000
# knn_df['Sequential Scan Time(s)'] = knn_df['Sequential Scan Time(ms)'] / 1000

# Useful information for Range Query
print("=== Range Query Analysis ===")
print(f"Total iterations: {len(range_df)}")
print(f"Average R* Time: {range_df['R* Time(ms)'].mean():.2f} ms")
print(f"Average Sequential Time: {range_df['Sequential Scan Time(ms)'].mean():.2f} ms")
print(
    f"Average Speedup (Seq / R*): {range_df['Sequential Scan Time(ms)'].mean() / range_df['R* Time(ms)'].mean():.2f}x"
)
print(f"Max Returned Records: {range_df['Returned Records'].max()}")
print(f"Min Returned Records: {range_df['Returned Records'].min()}")

# Calculate speedup ratio
range_df["Speedup"] = range_df["Sequential Scan Time(ms)"] / range_df["R* Time(ms)"]

# Plot for Range Query
plt.figure(figsize=(12, 6))
sns.lineplot(
    data=range_df,
    x="Returned Records",
    y="R* Time(ms)",
    label="R* Tree Time",
    marker="o",
)
sns.lineplot(
    data=range_df,
    x="Returned Records",
    y="Sequential Scan Time(ms)",
    label="Sequential Scan Time",
    marker="x",
)
plt.title("Range Query: R* Tree vs Sequential Scan Time")
plt.xlabel("Returned Records")
plt.ylabel("Time (ms)")
plt.legend()
plt.grid(True)
plt.savefig("range_query_comparison.png")
plt.show()

# Plot speedup
plt.figure(figsize=(12, 6))
sns.lineplot(
    data=range_df, x="Returned Records", y="Speedup", marker="o", color="green"
)
plt.title("Range Query Speedup (Sequential / R*)")
plt.xlabel("Returned Records")
plt.ylabel("Speedup Factor")
plt.grid(True)
plt.savefig("range_query_speedup.png")
plt.show()

# Useful information for KNN Query
print("\n=== KNN Query Analysis ===")
print(f"Total iterations: {len(knn_df)}")
print(f"Average R* Time: {knn_df['R* Time(ms)'].mean():.2f} ms")
print(f"Average Sequential Time: {knn_df['Sequential Scan Time(ms)'].mean():.2f} ms")
print(
    f"Average Speedup (Seq / R*): {knn_df['Sequential Scan Time(ms)'].mean() / knn_df['R* Time(ms)'].mean():.2f}x"
)
print(f"Max Returned Records: {knn_df['Returned Records'].max()}")
print(f"Min Returned Records: {knn_df['Returned Records'].min()}")

# Calculate speedup ratio for KNN
knn_df["Speedup"] = knn_df["Sequential Scan Time(ms)"] / knn_df["R* Time(ms)"]

# Plot for KNN Query
plt.figure(figsize=(12, 6))
sns.lineplot(
    data=knn_df, x="Returned Records", y="R* Time(ms)", label="R* Tree Time", marker="o"
)
sns.lineplot(
    data=knn_df,
    x="Returned Records",
    y="Sequential Scan Time(ms)",
    label="Sequential Scan Time",
    marker="x",
)
plt.title("KNN Query: R* Tree vs Sequential Scan Time")
plt.xlabel("Returned Records (k)")
plt.ylabel("Time (ms)")
plt.legend()
plt.grid(True)
plt.savefig("knn_query_comparison.png")
plt.show()

# Plot speedup for KNN
plt.figure(figsize=(12, 6))
sns.lineplot(data=knn_df, x="Returned Records", y="Speedup", marker="o", color="green")
plt.title("KNN Query Speedup (Sequential / R*)")
plt.xlabel("Returned Records (k)")
plt.ylabel("Speedup Factor")
plt.grid(True)
plt.savefig("knn_query_speedup.png")
plt.show()

print(
    "\nGraphs saved as PNG files: range_query_comparison.png, range_query_speedup.png, knn_query_comparison.png, knn_query_speedup.png"
)
